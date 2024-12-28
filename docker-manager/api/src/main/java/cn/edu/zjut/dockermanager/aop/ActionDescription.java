package cn.edu.zjut.dockermanager.aop;

import java.util.HashMap;
import java.util.Map;

public class ActionDescription {

    private static final Map<String, String> actionMap = new HashMap<>();

    static {
        // 这里根据你的 Controller 方法名映射对应的中文操作
        actionMap.put("getCpuUsage", "获取容器CPU使用率");
        actionMap.put("getMemoryUsage", "获取容器内存使用率");
        actionMap.put("createContainer", "创建容器");
        actionMap.put("getVncPort", "获取容器VNC端口");
        actionMap.put("getContainerStatusList", "获取容器状态列表");
        actionMap.put("getContainerConfigList", "获取容器配置列表");
        actionMap.put("getContainerConfig", "获取容器配置");
        actionMap.put("saveContainer", "保存容器配置");
        actionMap.put("start", "启动容器");
        actionMap.put("stop", "停止容器");
        actionMap.put("restart", "重启容器");
    }

    // 根据方法名获取中文操作描述
    public static String getActionDescription(String methodName) {
        return actionMap.getOrDefault(methodName, "未知操作");
    }
}
