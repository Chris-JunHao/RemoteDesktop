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
        // 创建一个新的 Container 对象，用于保存解析后的容器状态信息
        Container container = new Container();

        // 从 configV2 中获取容器 ID 和名称，并设置到 container 对象
        container.setId(configV2.getString("ID"));
        container.setName(configV2.getString("Name").substring(1));  // 去除名称前的斜杠 '/'

        // 获取容器的运行状态
        JSONObject state = configV2.getJSONObject("State");
        if (state.getBoolean("Running")) {
            container.setState("运行中");  // 如果容器正在运行
        } else if (state.getBoolean("Paused")) {
            container.setState("暂停中");  // 如果容器已暂停
        } else if (state.getBoolean("Restarting")) {
            container.setState("重启中");  // 如果容器正在重启
        } else {
            container.setState("已停止");  // 如果容器已经停止
        }

        // 解析容器启动时间
        try {
            String dateString = state.getString("StartedAt").replace("T", " ");  // 将启动时间中的 'T' 替换为空格
            dateString = dateString.substring(0, dateString.lastIndexOf("."));  // 去掉最后的时间精度部分
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  // 设置日期格式
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));  // 设置时区为 UTC
            container.setStartTime(sdf.parse(dateString));  // 解析时间字符串并设置为容器的启动时间
        } catch (ParseException e) {
            e.printStackTrace();  // 如果解析失败，打印异常堆栈
        }

        // 获取容器使用的镜像信息
        JSONObject config = configV2.getJSONObject("Config");
        String image = config.getString("Image");
        if (!image.contains(":")) {
            image += ":latest";  // 如果镜像名没有版本标签，默认为 'latest'
        }
        container.setImage(image);  // 设置容器镜像信息

        return container;  // 返回填充好的 container 对象
    }

    @Override
    public Container parseConfig(JSONObject configV2, JSONObject hostConfig) {
        // 创建一个新的 Container 对象，用于保存解析后的容器配置信息
        Container container = new Container();

        // 从 configV2 中获取容器 ID 和名称，并设置到 container 对象
        container.setId(configV2.getString("ID"));
        container.setName(configV2.getString("Name").substring(1));  // 去除名称前的斜杠 '/'

        // 获取容器的运行状态
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

        // 获取容器环境变量
        JSONObject config = configV2.getJSONObject("Config");
        JSONArray envArray = config.getJSONArray("Env");

        List<Container.Env> envList = new ArrayList<>();
        if (!envArray.isEmpty()) {
            for (int i = 0; i < envArray.size(); i++) {
                Container.Env env = new Container.Env();
                String envString = envArray.getString(i);
                String[] con = envString.split("=");  // 环境变量是以 'key=value' 格式存在的
                env.setKey(con[0]);  // 设置环境变量的键
                env.setValue(con.length == 1 ? "" : con[1]);  // 如果没有值，默认为空字符串
                envList.add(env);  // 添加到环境变量列表
            }
        }
        container.setEnvList(envList);  // 设置容器的环境变量列表

        // 获取容器挂载点信息
        List<Container.MountPoint> mountPointList = new ArrayList<>();
        JSONObject mountPoints = configV2.getJSONObject("MountPoints");
        Set<String> mountPointsKeySet = mountPoints.keySet();
        if (!mountPoints.isEmpty()) {
            for (String key : mountPointsKeySet) {
                JSONObject jsonObject = mountPoints.getJSONObject(key);
                Container.MountPoint mountPoint = new Container.MountPoint();
                mountPoint.setSource(jsonObject.getString("Source"));  // 设置源路径
                mountPoint.setTarget(jsonObject.getString("Destination"));  // 设置目标路径
                mountPoint.setReadOnly(!jsonObject.getBoolean("RW"));  // 设置是否为只读
                mountPointList.add(mountPoint);  // 添加到挂载点列表
            }
        }
        container.setMountPointList(mountPointList);  // 设置容器的挂载点列表

        // 获取容器端口映射信息
        JSONObject portBinds = hostConfig.getJSONObject("PortBindings");
        List<Container.PortBinding> portBindingList = new ArrayList<>();
        Set<String> portBindsKeySet = portBinds.keySet();
        if (!portBindsKeySet.isEmpty()) {
            for (String key : portBindsKeySet) {
                Container.PortBinding portBinding = new Container.PortBinding();
                String port = key.split("/")[0];  // 提取容器端口
                String type = key.split("/")[1];  // 提取端口类型（如 tcp 或 udp）
                String hostPort = portBinds.getJSONArray(key).getJSONObject(0).getString("HostPort");  // 获取主机端口
                portBinding.setPort(port);  // 设置容器端口
                portBinding.setHostPort(hostPort);  // 设置主机端口
                portBinding.setType(type);  // 设置端口类型
                portBindingList.add(portBinding);  // 添加到端口映射列表
            }
        }
        container.setPortBindingList(portBindingList);  // 设置容器的端口映射列表

        // 获取容器的网络模式
        String networkMode = hostConfig.getString("NetworkMode");
        container.setNetworkMode(networkMode);  // 设置容器的网络模式

        // 获取容器的重启策略
        String restartPolicy = hostConfig.getJSONObject("RestartPolicy").getString("Name");
        container.setRestartPolicy(restartPolicy);  // 设置容器的重启策略

        return container;  // 返回填充好的 container 对象
    }

    @Override
    public Container getContainer(String id) {
        // 读取指定 ID 的容器配置和主机配置
        JSONObject configV2 = DockerUtil.readConfigV2(id);
        JSONObject hostConfig = DockerUtil.readHostConfig(id);

        // 如果配置不为空，解析并返回容器信息
        if (null != configV2 && null != hostConfig) {
            return parseConfig(configV2, hostConfig);
        }
        return null;  // 配置为空时返回 null
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
        // 获取 Docker 容器目录下的所有容器文件
        File[] containers = DockerUtil.getContainerFiles();

        // 创建一个空的容器列表，用于存放解析后的容器对象
        List<Container> containerList = new ArrayList<>();

        // 检查容器文件数组是否为空
        if (FileUtil.isNotEmpty(containers)) {
            // 遍历容器目录中的每个文件
            for (File container : containers) {
                // 只处理文件夹，容器的每个实例应该是一个目录
                if (container.isDirectory()) {
                    // 获取容器的 ID（目录的名称即容器的 ID）
                    String id = container.getName();

                    // 读取容器的配置文件和主机配置文件
                    JSONObject configV2 = DockerUtil.readConfigV2(id);
                    JSONObject hostConfig = DockerUtil.readHostConfig(id);

                    // 解析容器配置，并将解析结果封装为 Container 对象
                    Container ctn = parseConfig(configV2, hostConfig);

                    // 将解析后的容器对象添加到容器列表中
                    containerList.add(ctn);
                }
            }
        } else {
            // 如果容器目录为空，则抛出路径错误异常
            throw new BusinessException(ErrorCodeEnum.PATH_ERROR);
        }

        // 返回解析后的容器列表
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
