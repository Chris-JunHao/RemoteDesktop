package cn.edu.zjut.dockermanager.controller;

import cn.edu.zjut.dockermanager.base.ResultBean;
import cn.edu.zjut.dockermanager.entity.Container;
import cn.edu.zjut.dockermanager.enums.ErrorCodeEnum;
import cn.edu.zjut.dockermanager.service.ContainerService;
import cn.edu.zjut.dockermanager.constant.Constant;
import cn.edu.zjut.dockermanager.exception.BusinessException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 容器管理控制器
 * 提供容器状态、配置的获取与操作接口
 */
@Slf4j  // 使用Slf4j日志
@RestController  // 标识为RESTful控制器
@RequestMapping("container")  // 设置容器相关请求的统一前缀
public class ContainerController {

    @Autowired  // 自动注入容器服务
    private ContainerService containerService;

    /**
     * 获取容器noVNC端口
     * @return
     */
    @GetMapping("getVncPort")
    public ResultBean getVncPort(String id){
        // 参数为空时抛出异常
        if (StringUtils.isEmpty(id)) {
            throw new BusinessException(ErrorCodeEnum.PARAMETER_EMPTY);  // 抛出空参数异常
        }
        // 获取容器信息
        Container container = containerService.getContainer(id);
        // 容器不存在时抛出异常
        if (null == container) {
            throw new BusinessException("容器id" + id + "不存在");  // 抛出容器不存在异常
        }
        String vncPort=containerService.getVncPort(id);
        // 6080内部端口不存在时抛出异常
        if (null == vncPort) {
            throw new BusinessException("noVNC端口不存在");  // 抛出端口不存在异常
        }
        return new ResultBean<>(vncPort);
    }

    /**
     * 获取容器状态列表
     * @return ResultBean 返回容器状态列表的ResultBean对象
     */
    @GetMapping("getContainerStatusList")  // 映射GET请求到"/container/getContainerStatusList"
    public ResultBean getContainerStatusList() {
        return new ResultBean<>(containerService.getContainerStatusList());  // 调用服务层获取容器状态并返回
    }

    /**
     * 获取所有容器配置列表
     * @return ResultBean 返回所有容器配置的ResultBean对象
     */
    @GetMapping("getContainerConfigList")  // 映射GET请求到"/container/getContainerConfigList"
    public ResultBean getContainerConfigList() {
        return new ResultBean<>(containerService.getContainerConfigList());  // 调用服务层获取所有容器配置并返回
    }

    /**
     * 获取指定容器的配置
     * @param id 容器ID
     * @return ResultBean 返回指定容器配置的ResultBean对象
     */
    @GetMapping("getContainerConfig")  // 映射GET请求到"/container/getContainerConfig"
    public ResultBean getContainerConfig(String id) {
        // 参数为空时抛出异常
        if (StringUtils.isEmpty(id)) {
            throw new BusinessException(ErrorCodeEnum.PARAMETER_EMPTY);  // 抛出空参数异常
        }

        // 获取容器信息
        Container container = containerService.getContainer(id);
        // 容器不存在时抛出异常
        if (null == container) {
            throw new BusinessException("容器id" + id + "不存在");  // 抛出容器不存在异常
        }
        return new ResultBean<>(container);  // 返回容器配置
    }

    /**
     * 保存容器配置
     * @param container 容器配置对象
     * @return ResultBean 返回操作结果
     */
    @PostMapping("saveContainer")  // 映射POST请求到"/container/saveContainer"
    public ResultBean saveContainer(@RequestBody Container container) {
        // 参数为空或容器ID为空时抛出异常
        if (null == container || null == container.getId()) {
            throw new BusinessException(ErrorCodeEnum.PARAMETER_EMPTY);  // 抛出空参数异常
        }
        containerService.saveContainer(container);  // 调用服务层保存容器配置
        return new ResultBean<>();  // 返回成功的结果
    }

    /**
     * 启动容器
     * @param params 包含容器ID的参数
     * @return ResultBean 返回操作结果
     */
    @PostMapping("start")  // 映射POST请求到"/container/start"
    public ResultBean start(@RequestBody JSONObject params) {
        String id = params.getString("id");  // 获取容器ID
        String res = containerService.startContainer(id);  // 调用服务层启动容器
        // 如果启动失败，返回错误信息
        if (res.contains(Constant.CONTAINER_ERROR)) {
            return ResultBean.error(res);  // 返回错误信息
        }
        return new ResultBean<>(res);  // 返回启动结果
    }

    /**
     * 停止容器
     * @param params 包含容器ID的参数
     * @return ResultBean 返回操作结果
     */
    @PostMapping("stop")  // 映射POST请求到"/container/stop"
    public ResultBean stop(@RequestBody JSONObject params) {
        String id = params.getString("id");  // 获取容器ID
        String res = containerService.stopContainer(id);  // 调用服务层停止容器
        // 如果停止失败，返回错误信息
        if (res.contains(Constant.CONTAINER_ERROR)) {
            return ResultBean.error(res);  // 返回错误信息
        }
        return new ResultBean<>(res);  // 返回停止结果
    }

    /**
     * 重启容器
     * @param params 包含容器ID的参数
     * @return ResultBean 返回操作结果
     */
    @PostMapping("restart")  // 映射POST请求到"/container/restart"
    public ResultBean restart(@RequestBody JSONObject params) {
        String id = params.getString("id");  // 获取容器ID
        String res = containerService.restartContainer(id);  // 调用服务层重启容器
        // 如果重启失败，返回错误信息
        if (res.contains(Constant.CONTAINER_ERROR)) {
            return ResultBean.error(res);  // 返回错误信息
        }
        return new ResultBean<>(res);  // 返回重启结果
    }
}
