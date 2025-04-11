package cn.edu.zjut.dockermanager.controller;

import cn.edu.zjut.dockermanager.base.ResultBean;
import cn.edu.zjut.dockermanager.constant.Constant;
import cn.edu.zjut.dockermanager.exception.BusinessException;
import cn.edu.zjut.dockermanager.service.DockerService;
import cn.edu.zjut.dockermanager.util.SystemUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Docker管理控制器
 * 提供Docker相关的操作接口，包括启动、停止、重启及获取Docker信息等
 */
@RestController  // 标识为RESTful控制器
@RequestMapping("docker")  // 设置Docker相关请求的统一前缀
public class DockerController {

    @Autowired  // 自动注入DockerService
    private DockerService dockerService;

    /**
     * 获取Docker的基本信息
     * @return ResultBean 返回操作结果，若Docker未启动则抛出异常
     */
    @GetMapping("info")  // 映射GET请求到"/docker/info"
    public ResultBean info() {
        // 执行docker info命令，获取Docker的基本信息
        String res = SystemUtil.execStr("docker info");
        // 如果无法连接到Docker，抛出业务异常
        if (res.contains(Constant.DOCKER_ERROR)) {
            throw new BusinessException("请先启动docker");  // 抛出Docker未启动异常
        }
        return new ResultBean();  // 返回成功的结果
    }

    /**
     * 启动Docker
     * @return ResultBean 返回启动Docker后的结果
     */
    @PostMapping("start")  // 映射POST请求到"/docker/start"
    public ResultBean start() {
        // 调用服务层启动Docker并返回结果
        return new ResultBean<>(dockerService.startDocker());
    }

    /**
     * 停止Docker
     * @return ResultBean 返回停止Docker后的结果
     */
    @PostMapping("stop")  // 映射POST请求到"/docker/stop"
    public ResultBean stop() {
        // 调用服务层停止Docker并返回结果
        return new ResultBean<>(dockerService.stopDocker());
    }

    /**
     * 重启Docker
     * @return ResultBean 返回重启Docker后的结果
     */
    @PostMapping("restart")  // 映射POST请求到"/docker/restart"
    public ResultBean restartDocker() {
        // 调用服务层重启Docker并返回结果
        return new ResultBean<>(dockerService.restartDocker());
    }
}
