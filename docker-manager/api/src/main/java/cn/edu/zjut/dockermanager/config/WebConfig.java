package cn.edu.zjut.dockermanager.config;

import cn.edu.zjut.dockermanager.filter.CorsFilter;
import cn.edu.zjut.dockermanager.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

@Configuration  // 声明该类为Spring配置类
public class WebConfig implements WebMvcConfigurer {
    @Autowired  // 自动注入LoginInterceptor
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册LoginInterceptor，拦截所有请求路径
        registry.addInterceptor(this.loginInterceptor).addPathPatterns("/**")
                // 排除Swagger相关页面和静态资源路径
                .excludePathPatterns("/doc.html")
                .excludePathPatterns("/swagger-resources/**")
                .excludePathPatterns("/v2/api-docs")
                .excludePathPatterns("/v2/api-docs-ext")
                .excludePathPatterns("/webjars/**")
                // 排除根路径、登录页面、静态资源等路径
                .excludePathPatterns("/")
                .excludePathPatterns("/login")
                .excludePathPatterns("/index.html")
                .excludePathPatterns("/static/**")
                .excludePathPatterns("/favicon.ico")
                .excludePathPatterns("/error");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 设置静态资源路径，确保访问`/**`时能找到`classpath:/static/`目录下的资源
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Bean  // 将CorsFilter注册为Spring Bean
    public FilterRegistrationBean<Filter> filterRegistrationBean() {
        // 创建FilterRegistrationBean，注册CorsFilter
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new CorsFilter());  // 设置过滤器为CorsFilter
        bean.addUrlPatterns("/*");  // 配置过滤器的URL模式
        return bean;
    }
}
