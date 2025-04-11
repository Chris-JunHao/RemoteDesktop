package cn.edu.zjut.dockermanager.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 容器实体类
 * 用于表示Docker容器的各种配置信息，包括ID、名称、状态、映像等信息
 */
@Data  // 自动生成getter、setter、toString等方法
public class Container {

    private String id;  // 容器ID

    private String name;  // 容器名称

    private String state;  // 容器状态（如：running, stopped）

    private String image;  // 容器使用的Docker镜像

    private String networkMode;  // 容器的网络模式

    private String restartPolicy;  // 容器的重启策略

    private Date startTime;  // 容器启动时间

    private List<PortBinding> portBindingList;  // 容器的端口映射列表

    private List<MountPoint> mountPointList;  // 容器的挂载点列表

    private List<Env> envList;  // 容器的环境变量列表

    /**
     * 端口映射类
     * 用于描述容器端口和主机端口的绑定关系
     */
    @Data  // 自动生成getter、setter、toString等方法
    public static class PortBinding {

        private String type;  // 端口类型（如：tcp）

        private String port;  // 容器端口

        private String hostPort;  // 主机端口
    }

    /**
     * 挂载点类
     * 用于描述容器内外文件系统的挂载关系
     */
    @Data  // 自动生成getter、setter、toString等方法
    public static class MountPoint {

        private String source;  // 源路径（主机上的路径）

        private String target;  // 目标路径（容器中的路径）

        private Boolean readOnly;  // 是否为只读挂载
    }

    /**
     * 环境变量类
     * 用于描述容器中的环境变量（键值对）
     */
    @Data  // 自动生成getter、setter、toString等方法
    public static class Env {

        private String key;  // 环境变量的键

        private String value;  // 环境变量的值
    }
}
