package cn.edu.zjut.dockermanager.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

public class DockerUtil {

    // 标记是否使用 systemd
    public static boolean systemd = true;

    // 容器路径，默认为 Docker 默认容器路径
    public static String containerPath = "/var/lib/docker/containers";

    // 配置文件名
    private final static String CONFIG_V2_FILE = "config.v2.json";
    private final static String HOST_CONFIG_FILE = "hostconfig.json";

    /**
     * 检查 Docker 配置是否正常
     * 通过执行 `docker info` 命令来检查 Docker 是否已安装并在运行。
     * @return boolean 如果 Docker 正常工作，返回 true；否则返回 false。
     * @author XanderYe
     * @date 2020/11/24
     */
    public static boolean checkDocker() {
        try {
            // 执行 Docker info 命令，检查 Docker 是否正常工作
            String res = SystemUtil.execStr("docker info");
            if (res.contains("command not found") || res.contains("Cannot connect to the Docker daemon")) {
                return false; // Docker 未安装或未运行
            }
            // 提取 Docker 根目录路径
            String dockerRootDir = StringUtils.substringBetween(res, "Docker Root Dir:", "Debug").trim();
            containerPath = dockerRootDir + File.separator + "containers";
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取所有容器的文件
     * 从容器存储路径中列出所有的容器文件。
     * @return java.io.File[] 返回容器目录下的所有文件。
     * @author XanderYe
     * @date 2020/11/25
     */
    public static File[] getContainerFiles() {
        File containerFile = new File(containerPath);
        return containerFile.listFiles();
    }

    /**
     * 读取配置文件内容
     * 读取给定路径的配置文件，并将其内容解析为 JSON 对象。
     * @param path 配置文件路径
     * @return com.alibaba.fastjson.JSONObject 返回解析后的 JSON 对象
     * @author XanderYe
     * @date 2020/11/24
     */
    public static JSONObject readConfig(String path) {
        File file = new File(path);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String s = bufferedReader.readLine();
            return JSON.parseObject(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 写入配置文件
     * 将 JSON 对象写入指定的配置文件路径。
     * @param jsonObject 要写入的 JSON 对象
     * @param path 配置文件路径
     * @return boolean 如果写入成功，返回 true；否则返回 false
     * @author XanderYe
     * @date 2020/11/24
     */
    public static boolean writeConfig(JSONObject jsonObject, String path) {
        File file = new File(path);
        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(jsonObject.toJSONString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据容器 ID 读取 config.v2.json 文件
     * 通过容器 ID 获取对应的 `config.v2.json` 文件内容。
     * @param id 容器 ID
     * @return com.alibaba.fastjson.JSONObject 返回配置文件内容
     * @author XanderYe
     * @date 2020/11/25
     */
    public static JSONObject readConfigV2(String id) {
        String configV2Path = containerPath + File.separator + id + File.separator + CONFIG_V2_FILE;
        return readConfig(configV2Path);
    }

    /**
     * 根据容器 ID 读取 hostconfig.json 文件
     * 通过容器 ID 获取对应的 `hostconfig.json` 文件内容。
     * @param id 容器 ID
     * @return com.alibaba.fastjson.JSONObject 返回配置文件内容
     * @author XanderYe
     * @date 2020/11/25
     */
    public static JSONObject readHostConfig(String id) {
        String hostConfigPath = containerPath + File.separator + id + File.separator + HOST_CONFIG_FILE;
        return readConfig(hostConfigPath);
    }

    /**
     * 写入 config.v2.json 文件
     * 将修改后的 JSON 对象写入到容器的 `config.v2.json` 文件。
     * @param id 容器 ID
     * @param configV2 修改后的 config.v2.json 配置内容
     * @return boolean 如果写入成功，返回 true；否则返回 false
     * @author XanderYe
     * @date 2020/11/25
     */
    public static boolean writeConfigV2(String id, JSONObject configV2) {
        String configV2Path = containerPath + File.separator + id + File.separator + CONFIG_V2_FILE;
        try {
            FileUtil.copyFile(configV2Path, configV2Path + ".bak");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writeConfig(configV2, configV2Path);
    }

    /**
     * 写入 hostconfig.json 文件
     * 将修改后的 JSON 对象写入到容器的 `hostconfig.json` 文件。
     * @param id 容器 ID
     * @param hostConfig 修改后的 hostconfig.json 配置内容
     * @return boolean 如果写入成功，返回 true；否则返回 false
     * @author XanderYe
     * @date 2020/11/25
     */
    public static boolean writeHostConfig(String id, JSONObject hostConfig) {
        String hostConfigPath = containerPath + File.separator + id + File.separator + HOST_CONFIG_FILE;
        try {
            FileUtil.copyFile(hostConfigPath, hostConfigPath + ".bak");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writeConfig(hostConfig, hostConfigPath);
    }
}
