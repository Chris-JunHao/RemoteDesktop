package cn.edu.zjut.dockermanager.service.impl;

import cn.edu.zjut.dockermanager.entity.Container;
import cn.edu.zjut.dockermanager.enums.ErrorCodeEnum;
import cn.edu.zjut.dockermanager.exception.BusinessException;
import cn.edu.zjut.dockermanager.service.ContainerService;
import cn.edu.zjut.dockermanager.util.DockerUtil;
import cn.edu.zjut.dockermanager.util.FileUtil;
import cn.edu.zjut.dockermanager.util.SystemUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class ContainerServiceImpl implements ContainerService {

    @Override
    public Container parseStatus(JSONObject configV2, JSONObject hostConfig) {
        Container container = new Container();
        container.setId(configV2.getString("ID"));
        container.setName(configV2.getString("Name").substring(1));
        JSONObject state = configV2.getJSONObject("State");
        if (state.getBoolean("Running")) {
            container.setState("运行中");
        } else if (state.getBoolean("Paused")) {
            container.setState("暂停中");
        } else if (state.getBoolean("Restarting")) {
            container.setState("重启中");
        } else {
            container.setState("已停止");
        }
        try {
            String dateString = state.getString("StartedAt").replace("T", " ");
            dateString = dateString.substring(0, dateString.lastIndexOf("."));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            container.setStartTime(sdf.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject config = configV2.getJSONObject("Config");
        String image = config.getString("Image");
        if (!image.contains(":")) {
            image += ":latest";
        }
        container.setImage(image);
        return container;
    }

    @Override
    public Container parseConfig(JSONObject configV2, JSONObject hostConfig) {
        Container container = new Container();
        container.setId(configV2.getString("ID"));
        container.setName(configV2.getString("Name").substring(1));
        JSONObject state = configV2.getJSONObject("State");
        if (state.getBoolean("Running")) {
            container.setState("运行中");
        } else if (state.getBoolean("Paused")) {
            container.setState("暂停中");
        } else if (state.getBoolean("Restarting")) {
            container.setState("重启中");
        } else {
            container.setState("已停止");
        }

        JSONObject config = configV2.getJSONObject("Config");
        JSONArray envArray = config.getJSONArray("Env");

        // 获取环境变量
        List<Container.Env> envList = new ArrayList<>();
        if (!envArray.isEmpty()) {
            for (int i = 0; i < envArray.size(); i++) {
                Container.Env env = new Container.Env();
                String envString = envArray.getString(i);
                String[] con = envString.split("=");
                env.setKey(con[0]);
                env.setValue(con.length == 1 ? "" : con[1]);
                envList.add(env);
            }
        }
        container.setEnvList(envList);

        // 获取映射目录
        List<Container.MountPoint> mountPointList = new ArrayList<>();
        JSONObject mountPoints = configV2.getJSONObject("MountPoints");
        Set<String> mountPointsKeySet = mountPoints.keySet();
        if (!mountPoints.isEmpty()) {
            for (String key : mountPointsKeySet) {
                JSONObject jsonObject = mountPoints.getJSONObject(key);
                Container.MountPoint mountPoint = new Container.MountPoint();
                mountPoint.setSource(jsonObject.getString("Source"));
                mountPoint.setTarget(jsonObject.getString("Destination"));
                mountPoint.setReadOnly(!jsonObject.getBoolean("RW"));
                mountPointList.add(mountPoint);
            }
        }
        container.setMountPointList(mountPointList);

        // 获取映射端口
        JSONObject portBinds = hostConfig.getJSONObject("PortBindings");
        List<Container.PortBinding> portBindingList = new ArrayList<>();
        Set<String> portBindsKeySet = portBinds.keySet();
        if (!portBindsKeySet.isEmpty()) {
            for (String key : portBindsKeySet) {
                Container.PortBinding portBinding = new Container.PortBinding();
                String port = key.split("/")[0];
                String type = key.split("/")[1];
                String hostPort = portBinds.getJSONArray(key).getJSONObject(0).getString("HostPort");
                portBinding.setPort(port);
                portBinding.setHostPort(hostPort);
                portBinding.setType(type);
                portBindingList.add(portBinding);
            }
        }
        container.setPortBindingList(portBindingList);

        // 获取容器网络模式
        String networkMode = hostConfig.getString("NetworkMode");
        container.setNetworkMode(networkMode);

        // 获取容器是否自动重启
        String restartPolicy = hostConfig.getJSONObject("RestartPolicy").getString("Name");
        container.setRestartPolicy(restartPolicy);
        return container;
    }

    @Override
    public Container getContainer(String id) {
        JSONObject configV2 = DockerUtil.readConfigV2(id);
        JSONObject hostConfig = DockerUtil.readHostConfig(id);
        if (null != configV2 && null != hostConfig) {
            return parseConfig(configV2, hostConfig);
        }
        return null;
    }

    @Override
    public List<Container> getContainerStatusList() {
        // 获取 Docker 容器的文件列表
        File[] containers = DockerUtil.getContainerFiles();
        // 创建一个容器列表，用于存储解析后的容器对象
        List<Container> containerList = new ArrayList<>();
        // 检查容器文件列表是否为空，如果不为空则继续处理
        if (FileUtil.isNotEmpty(containers)) {
            // 遍历每个容器文件
            for (File container : containers) {
                // 判断该文件是否为目录（容器的配置目录通常是目录类型）
                if (container.isDirectory()) {
                    // 获取容器的 ID（容器文件夹的名称就是容器的 ID）
                    String id = container.getName();
                    // 读取容器的配置信息和主机配置信息
                    JSONObject configV2 = DockerUtil.readConfigV2(id);
                    JSONObject hostConfig = DockerUtil.readHostConfig(id);
                    // 解析容器的状态，并将状态信息封装到 Container 对象中
                    Container ctn = parseStatus(configV2, hostConfig);
                    // 将解析得到的容器对象添加到容器列表中
                    containerList.add(ctn);
                }
            }
        } else {
            // 如果容器文件列表为空，抛出业务异常
            throw new BusinessException(ErrorCodeEnum.PATH_ERROR);
        }
        // 返回容器列表
        return containerList;
    }

    @Override
    public List<Container> getContainerConfigList() {
        File[] containers = DockerUtil.getContainerFiles();
        List<Container> containerList = new ArrayList<>();
        if (FileUtil.isNotEmpty(containers)) {
            for (File container : containers) {
                if (container.isDirectory()) {
                    String id = container.getName();
                    JSONObject configV2 = DockerUtil.readConfigV2(id);
                    JSONObject hostConfig = DockerUtil.readHostConfig(id);
                    Container ctn = parseConfig(configV2, hostConfig);
                    containerList.add(ctn);
                }
            }
        } else {
            throw new BusinessException(ErrorCodeEnum.PATH_ERROR);
        }
        return containerList;
    }

    @Override
    public void saveContainer(Container container) {
        String id = container.getId();
        // 读取当前容器的配置文件 config.v2.json 和 hostconfig.json
        JSONObject configV2 = DockerUtil.readConfigV2(id);
        JSONObject hostConfig = DockerUtil.readHostConfig(id);
        // 获取配置中的 "Config" 节点
        JSONObject config = configV2.getJSONObject("Config");
        // 设置环境变量
        List<Container.Env> envList = container.getEnvList();
        JSONArray envArray = new JSONArray();
        if (null != envList && !envList.isEmpty()) {
            for (Container.Env env : envList) {
                // 将每个环境变量以 "key=value" 的形式加入到数组中
                envArray.add(env.getKey() + "=" + env.getValue());
            }
        }
        // 更新 Config 中的 "Env" 节点
        config.put("Env", envArray);
        // 设置挂载路径
        List<Container.MountPoint> mountPointList = container.getMountPointList();
        JSONObject configMountPoints = new JSONObject(true); // 挂载点配置（用于 config.v2.json）
        JSONArray hostConfigBinds = new JSONArray(); // 挂载绑定信息（用于 hostconfig.json）
        if (null != mountPointList && !mountPointList.isEmpty()) {
            for (Container.MountPoint mountPoint : mountPointList) {
                // 配置挂载点信息到 config.v2.json
                JSONObject conf = new JSONObject(true);
                conf.put("Source", mountPoint.getSource()); // 主机路径
                conf.put("Destination", mountPoint.getTarget()); // 容器路径
                conf.put("Name", "");
                conf.put("Driver", "");
                conf.put("Type", "bind");
                conf.put("Propagation", "rprivate");
                JSONObject spec = new JSONObject(true);
                spec.put("Type", "bind");
                spec.put("Source", mountPoint.getSource());
                spec.put("Target", mountPoint.getTarget());
                if (mountPoint.getReadOnly()) {
                    // 配置只读属性
                    conf.put("RW", false);
                    conf.put("Relabel", "ro");
                    spec.put("ReadOnly", true);
                } else {
                    // 配置可读写属性
                    conf.put("RW", true);
                    conf.put("Relabel", "rw");
                }
                conf.put("Spec", spec);
                conf.put("SkipMountpointCreation", false);
                configMountPoints.put(mountPoint.getTarget(), conf);

                // 配置挂载点信息到 hostconfig.json
                hostConfigBinds.add(mountPoint.getSource() + ":" + mountPoint.getTarget());
            }
        }
        // 更新 config.v2.json 的挂载点信息
        configV2.put("MountPoints", configMountPoints);
        // 更新 hostconfig.json 的绑定挂载点信息
        hostConfig.put("Binds", hostConfigBinds);

        // 设置端口映射
        List<Container.PortBinding> portBindingList = container.getPortBindingList();
        JSONObject portBindings = new JSONObject(true); // 端口绑定信息（用于 hostconfig.json）
        JSONObject exposedPorts = new JSONObject(true); // 暴露的端口信息（用于 config.v2.json）
        if (null != portBindingList && !portBindingList.isEmpty()) {
            for (Container.PortBinding portBinding : portBindingList) {
                // 配置暴露端口到 config.v2.json
                String name = portBinding.getPort() + "/" + portBinding.getType();
                exposedPorts.put(name, new JSONObject());

                // 配置端口绑定到 hostconfig.json
                JSONObject host = new JSONObject(true);
                host.put("HostIp", ""); // 主机 IP 地址，留空表示绑定到所有 IP
                host.put("HostPort", portBinding.getHostPort()); // 主机端口
                JSONArray hostArray = new JSONArray();
                hostArray.add(host);
                portBindings.put(name, hostArray);
            }
        }
        // 更新 config.v2.json 的暴露端口信息
        config.put("ExposedPorts", exposedPorts);
        // 更新 hostconfig.json 的端口绑定信息
        hostConfig.put("PortBindings", portBindings);

        // 设置容器自动重启策略
        JSONObject restartPolicy = hostConfig.getJSONObject("RestartPolicy");
        restartPolicy.put("Name", container.getRestartPolicy());
        hostConfig.put("RestartPolicy", restartPolicy);

        // 更新 config.v2.json 的 Config 节点
        configV2.put("Config", config);

        // 判断是否需要先停止容器再修改配置
        boolean needStop = null == container.getState() || "Running".equals(container.getState());
        if (needStop) {
            // 修改配置前停止容器
            stopContainer(container.getId());
        }

        // 写入修改后的配置到对应文件
        DockerUtil.writeConfigV2(id, configV2);
        DockerUtil.writeHostConfig(id, hostConfig);

        if (needStop) {
            // 重启已停止的容器
            startContainer(container.getId());
        }
    }


    @Override
    public void saveContainerList(List<Container> containerList) {
        for (Container container : containerList) {
            saveContainer(container);
        }
    }

    @Override
    public String startContainer(String id) {
        return SystemUtil.execStr("docker start " + id);
    }

    @Override
    public String stopContainer(String id) {
        return SystemUtil.execStr("docker stop " + id);
    }

    @Override
    public String restartContainer(String id) {
        return SystemUtil.execStr("docker restart " + id);
    }

    @Override
    public String getVncPort(String id) {
        // 通过容器 ID 获取容器对象
        Container container = getContainer(id);
        List<Container.PortBinding> portBindings = container.getPortBindingList();
        // 遍历端口映射列表，寻找符合条件的映射
        for (Container.PortBinding binding : portBindings) {
            if ("tcp".equalsIgnoreCase(binding.getType()) && "6080".equals(binding.getPort())) {
                return binding.getHostPort();
            }
        }
        return null;
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 定义保留数据的最大时间，单位为毫秒 (40分钟)
    private static final long MAX_DATA_RETENTION_TIME = 40 * 60 * 1000;  // 40分钟

    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void collectCpuUsage() {
        long timestamp = System.currentTimeMillis(); // 获取当前时间戳
        String statsOutput = SystemUtil.execStr("docker stats --no-stream --format \"{{.Name}} {{.CPUPerc}}\"");

        for (String line : statsOutput.split("\n")) {
            String[] parts = line.split(" ");
            if (parts.length == 2) {
                String containerName = parts[0];
                String cpuUsage = parts[1];
                String redisKey = "container:cpu:" + containerName;
                redisTemplate.opsForHash().put(redisKey, String.valueOf(timestamp), cpuUsage);
                removeOldData(redisKey, timestamp); // 删除超过40分钟的数据
            }
        }
    }

    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void collectMemoryUsage() {
        long timestamp = System.currentTimeMillis(); // 获取当前时间戳
        String statsOutput = SystemUtil.execStr("docker stats --no-stream --format \"{{.Name}} {{.MemPerc}}\"");

        for (String line : statsOutput.split("\n")) {
            String[] parts = line.split(" ");
            if (parts.length == 2) {
                String containerName = parts[0];
                String memoryUsage = parts[1];
                String redisKey = "container:memory:" + containerName;
                redisTemplate.opsForHash().put(redisKey, String.valueOf(timestamp), memoryUsage);
                removeOldData(redisKey, timestamp); // 删除超过40分钟的数据
            }
        }
    }

    // 删除超过40分钟的数据
    private void removeOldData(String redisKey, long currentTimestamp) {
        Map<Object, Object> dataMap = redisTemplate.opsForHash().entries(redisKey);
        if (dataMap != null) {
            for (Map.Entry<Object, Object> entry : dataMap.entrySet()) {
                long storedTimestamp = Long.parseLong(entry.getKey().toString());
                if (currentTimestamp - storedTimestamp > MAX_DATA_RETENTION_TIME) {
                    redisTemplate.opsForHash().delete(redisKey, entry.getKey());
                }
            }
        }
    }

}
