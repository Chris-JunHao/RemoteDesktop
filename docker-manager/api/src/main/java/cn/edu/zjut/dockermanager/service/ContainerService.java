package cn.edu.zjut.dockermanager.service;

import cn.edu.zjut.dockermanager.entity.Container;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 容器服务接口
 * 提供了容器相关操作的抽象，包括容器的启动、停止、重启以及获取容器状态和配置等功能。
 */
public interface ContainerService {

    /**
     * 解析容器状态
     * 根据容器的配置和主机配置解析容器状态。
     * @param configV2 容器的配置
     * @param hostConfig 容器的主机配置
     * @return 返回解析后的容器状态对象
     * @author XanderYe
     * @date 2020/11/25
     */
    Container parseStatus(JSONObject configV2, JSONObject hostConfig);

    /**
     * 解析容器配置
     * 根据容器的配置和主机配置解析容器的详细配置信息。
     * @param configV2 容器的配置
     * @param hostConfig 容器的主机配置
     * @return 返回解析后的容器配置对象
     * @author XanderYe
     * @date 2020/11/25
     */
    Container parseConfig(JSONObject configV2, JSONObject hostConfig);

    /**
     * 根据容器id获取容器
     * 根据容器的唯一标识符（id）获取对应的容器对象。
     * @param id 容器的唯一标识符
     * @return 返回容器对象
     * @author XanderYe
     * @date 2020/11/25
     */
    Container getContainer(String id);

    /**
     * 获取所有容器的状态列表
     * 获取系统中所有容器的当前状态列表。
     * @return 返回所有容器的状态列表
     * @author XanderYe
     * @date 2020/11/25
     */
    List<Container> getContainerStatusList();

    /**
     * 获取所有容器的详细配置列表
     * 获取系统中所有容器的详细配置列表。
     * @return 返回所有容器的配置列表
     * @author XanderYe
     * @date 2020/11/25
     */
    List<Container> getContainerConfigList();

    /**
     * 保存容器配置
     * 保存单个容器的配置。
     * @param container 容器对象，包含了容器的详细配置
     * @return void
     * @author XanderYe
     * @date 2020/11/25
     */
    void saveContainer(Container container);

    /**
     * 批量保存容器配置
     * 批量保存多个容器的配置。
     * @param containerList 容器对象列表，包含了多个容器的详细配置
     * @return void
     * @author XanderYe
     * @date 2020/11/25
     */
    void saveContainerList(List<Container> containerList);

    /**
     * 启动容器
     * 根据容器的唯一标识符（id）启动指定容器。
     * @param id 容器的唯一标识符
     * @return 返回容器启动的结果消息
     * @author XanderYe
     * @date 2020/11/25
     */
    String startContainer(String id);

    /**
     * 停止容器
     * 根据容器的唯一标识符（id）停止指定容器。
     * @param id 容器的唯一标识符
     * @return 返回容器停止的结果消息
     * @author XanderYe
     * @date 2020/11/25
     */
    String stopContainer(String id);

    /**
     * 重启容器
     * 根据容器的唯一标识符（id）重启指定容器。
     * @param id 容器的唯一标识符
     * @return 返回容器重启的结果消息
     * @author XanderYe
     * @date 2020/11/25
     */
    String restartContainer(String id);
}
