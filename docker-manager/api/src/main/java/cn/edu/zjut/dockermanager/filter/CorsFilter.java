package cn.edu.zjut.dockermanager.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 跨域过滤器 (CORS Filter)
 * 该过滤器用于处理跨域请求，允许指定来源的跨域访问。
 * 它设置了相关的响应头，以确保前端能够与后端进行跨域通信。
 */
public class CorsFilter implements Filter {

    /**
     * 初始化方法
     * 可以在这里做一些初始化操作，但在此代码中不需要做任何事。
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // 暂时未用到初始化配置
    }

    /**
     * 过滤器的核心方法
     * 该方法会在每次请求到达服务器时被调用，用于设置跨域相关的响应头。
     * @param servletRequest 请求对象
     * @param servletResponse 响应对象
     * @param filterChain 过滤链
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 强制类型转换为HTTP请求和响应对象
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取请求的Origin头部信息（表示请求来源的域）
        String originHeader = request.getHeader("Origin");

        // 设置响应头，允许来自指定域的跨域请求
        response.setHeader("Access-Control-Allow-Origin", originHeader);
        // 设置允许的请求方法
        response.setHeader("Access-Control-Allow-Methods", "POST,GET,PUT,OPTIONS,DELETE");
        // 设置允许的请求头部信息
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,Origin,No-Cache,token,content-type");
        // 设置是否支持发送cookie跨域请求
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 继续执行过滤链中的下一个过滤器或目标资源
        filterChain.doFilter(request, response);
    }

    /**
     * 销毁方法
     * 可以在这里做一些清理工作，但在此代码中不需要做任何事。
     */
    @Override
    public void destroy() {
        // 暂时未用到销毁清理操作
    }
}
