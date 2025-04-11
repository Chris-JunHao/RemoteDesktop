package cn.edu.zjut.dockermanager.aop;

import cn.edu.zjut.dockermanager.enums.ErrorCodeEnum;
import cn.edu.zjut.dockermanager.exception.BusinessException;
import cn.edu.zjut.dockermanager.mapper.LogMapper;
import cn.edu.zjut.dockermanager.service.LogService;
import cn.edu.zjut.dockermanager.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import cn.edu.zjut.dockermanager.interceptor.LoginInterceptor;

@Slf4j  // 引入 SLF4J 日志
@Aspect  // 声明这是一个切面类
@Component  // 让 Spring 管理该切面类
public class ContainerControllerLogAspect {
    //TODO 可以完善一下，将日志数据操作封装到服务层中，同时使用jwt获取用户姓名，再给出操作容器名称
    @Autowired
    private LogService logService;
    // 切点：匹配 controller 包下所有类的所有方法
    @Pointcut("execution(public * cn.edu.zjut.dockermanager.controller..*(..)) && !execution(public * cn.edu.zjut.dockermanager.controller.ContainerController.getContainerStatusList(..))")
    public void controllerMethod() {}

    @Autowired
    private HttpServletRequest request;  // 用于获取请求中的 JWT

    /**
     * 在控制器方法执行后记录日志
     */
    @After("controllerMethod()")
    public void logAfter(JoinPoint joinPoint) {
        // 获取方法名
        String methodName = joinPoint.getSignature().getName();

        String username= "user";

        // 获取方法的中文描述
        String action = ActionDescription.getActionDescription(methodName);

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        Timestamp time = Timestamp.valueOf(now);  // 转换为 Timestamp 类型

        if(action.equals("未知操作")){
            return;
        }
        // 插入日志
        logService.insert(username, action, time);
    }


    // 异常通知：如果方法抛出异常，则输出异常信息
    @AfterThrowing(pointcut = "controllerMethod()", throwing = "e")
    public void logException(JoinPoint joinPoint, Throwable e) {
        // 获取方法名
        String methodName = joinPoint.getSignature().getName();
        // 打印异常日志
        log.error("Method {} threw exception: {}", methodName, e.getMessage());
    }
}
