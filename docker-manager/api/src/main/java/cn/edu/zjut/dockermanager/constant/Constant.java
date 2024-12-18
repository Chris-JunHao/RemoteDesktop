package cn.edu.zjut.dockermanager.constant;

import java.util.ArrayList;
import java.util.List;

public class Constant {

    // 系统连接错误信息常量
    public final static String SYSTEMD_ERROR = "Failed to connect";

    // Docker连接错误信息常量
    public final static String DOCKER_ERROR = "Cannot connect to the Docker daemon";

    // 容器操作错误信息常量
    public final static String CONTAINER_ERROR = "Error";

    // 被忽略的URI列表，用于过滤某些请求
    public final static List<String> IGNORED_URI_LIST = new ArrayList<String>() {{
        add("/container/getContainerStatusList");  // 将该URI加入忽略列表
    }};
}
