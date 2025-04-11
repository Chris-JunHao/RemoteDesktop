package cn.edu.zjut.dockermanager.interceptor;

import cn.edu.zjut.dockermanager.constant.Constant;
import cn.edu.zjut.dockermanager.enums.ErrorCodeEnum;
import cn.edu.zjut.dockermanager.exception.BusinessException;
import cn.edu.zjut.dockermanager.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 * 用于验证请求中的 token 是否有效，确保请求者已认证登录。
 * 拦截器会在请求处理前检查 token，确保请求是由经过认证的用户发起。
 */
@Slf4j
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    /**
     * 在请求处理之前执行
     * 该方法会验证请求头中的 token 是否有效，如果无效则抛出认证异常。
     * @param request 当前请求
     * @param response 当前响应
     * @param handler 处理器
     * @return true 表示继续执行后续的处理，false 表示请求被拦截，不继续执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求的来源 IP 地址、请求方法和请求的 URI
        String remoteAddr = request.getRemoteAddr();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 不拦截OPTIONS请求（CORS预检请求）
        if (method.equals(RequestMethod.OPTIONS.name())) {
            return true;
        }

        // 获取请求头中的 token
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            // 如果没有 token，抛出认证失败异常
            throw new BusinessException(ErrorCodeEnum.AUTHORIZATION_FAILED);
        }

        String username;
        try {
            // 解码 token 获取用户名
            username = JwtUtil.decode(token);
        } catch (Exception e) {
            // 如果 token 无效或解码失败，抛出认证失败异常
            throw new BusinessException(ErrorCodeEnum.AUTHORIZATION_FAILED);
        }

        // 如果请求的 URI 不在忽略的 URI 列表中，则记录请求信息
        if (!Constant.IGNORED_URI_LIST.contains(uri)) {
            log.info("remoteAddr={},  method={}, uri={}, username={}", remoteAddr, method, uri, username);
        }

        // 允许请求继续执行
        return true;
    }

    /**
     * 请求处理完成后的回调方法
     * 在请求完成后清理资源或进行其他操作
     * 该方法此时未做任何操作。
     * @param request 当前请求
     * @param response 当前响应
     * @param handler 处理器
     * @param ex 异常（如果有的话）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 不做任何操作
    }
}
