package cn.edu.zjut.dockermanager.enums;

import lombok.Getter;

/**
 * 错误码枚举类
 * 用于定义系统中可能出现的错误码及其对应的错误信息
 */
@Getter  // 自动生成getter方法
public enum ErrorCodeEnum {

    // 错误码定义
    LOGIN_ERROR(10000, "用户名或密码错误"),  // 登录错误：用户名或密码不正确
    AUTHORIZATION_FAILED(10001, "账号认证异常"),  // 授权失败：账号认证异常
    RUN_WITH_ROOT(10100, "请使用root账户启动"),  // 必须使用root账户启动
    INSTALL_DOCKER_FIRST(10101, "请先安装docker"),  // Docker未安装
    PATH_ERROR(10102, "路径配置错误，无法获取到容器"),  // 路径配置错误，无法获取到容器信息
    PARAMETER_ERROR(10103, "参数错误"),  // 参数错误
    PARAMETER_EMPTY(10104, "参数不为空");  // 参数不能为空

    private int code;  // 错误码
    private String message;  // 错误信息

    // 构造方法，用于初始化错误码和错误信息
    ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
