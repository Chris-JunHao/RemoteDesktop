package cn.edu.zjut.dockermanager.exception;

import cn.edu.zjut.dockermanager.base.ResultBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 全局异常处理类
 * 通过@ControllerAdvice捕获系统中所有未处理的异常，并返回统一的响应格式
 */
@Slf4j  // 使用SLF4J日志库进行日志记录
@ControllerAdvice  // 用于定义全局异常处理
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 处理Spring内部抛出的异常
     * @param e 异常对象
     * @param body 响应体
     * @param headers 请求头
     * @param status HTTP状态
     * @param request 请求对象
     * @return 包装异常信息的响应实体
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(e.getMessage(), e);  // 记录错误信息及堆栈信息
        return super.handleExceptionInternal(e, body, headers, status, request);  // 返回默认异常响应
    }

    /**
     * 处理自定义的业务异常
     * @param e 业务异常对象
     * @return 包装异常信息的响应实体
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleBusinessException(BusinessException e) {
        log.error(e.getMessage());  // 记录错误信息
        // 返回自定义错误码和消息的ResultBean
        return new ResponseEntity<>(new ResultBean(e.getCode(), e.getMessage()), HttpStatus.OK);
    }

    /**
     * 处理运行时异常
     * @param e 运行时异常对象
     * @return 包装错误信息的响应实体，HTTP状态码为500
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleRuntimeException(RuntimeException e) {
        log.error(e.getMessage(), e);  // 记录错误信息及堆栈信息
        // 返回500的错误响应
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理所有其他异常
     * @param e 一般异常对象
     * @return 包装错误信息的响应实体，HTTP状态码为500
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleException(Exception e) {
        log.error(e.getMessage(), e);  // 记录错误信息及堆栈信息
        // 返回500的错误响应
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
