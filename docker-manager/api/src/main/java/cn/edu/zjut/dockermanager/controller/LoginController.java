package cn.edu.zjut.dockermanager.controller;

import cn.edu.zjut.dockermanager.base.ResultBean;
import cn.edu.zjut.dockermanager.enums.ErrorCodeEnum;
import cn.edu.zjut.dockermanager.util.JwtUtil;
import cn.edu.zjut.dockermanager.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器
 * 提供用户登录验证功能，验证用户名和密码，成功后返回JWT令牌
 */
@RestController  // 标识为RESTful控制器
@RequestMapping  // 设置登录请求的统一前缀
public class LoginController {

    @Value("${user.username}")  // 从配置文件中读取用户名
    private String sysUsername;

    @Value("${user.password}")  // 从配置文件中读取密码
    private String sysPassword;

    /**
     * 用户登录接口
     * @param username 用户名
     * @param password 密码
     * @return ResultBean 返回JWT令牌
     */
    @PostMapping("login")  // 映射POST请求到"/login"
    public ResultBean login(String username, String password) {
        // 检查用户名和密码是否为空
        if (StringUtils.isAnyEmpty(username, password)) {
            throw new BusinessException(ErrorCodeEnum.PARAMETER_EMPTY);  // 如果为空，抛出空参数异常
        }

        // 验证用户名和密码是否匹配
        if (!sysUsername.equals(username) || !sysPassword.equals(password)) {
            throw new BusinessException(ErrorCodeEnum.LOGIN_ERROR);  // 如果用户名或密码不匹配，抛出登录错误异常
        }

        // 生成JWT令牌
        String jwt = JwtUtil.encode(username);
        return new ResultBean<>(jwt);  // 返回JWT令牌
    }
}
