package cn.edu.zjut.dockermanager.exception;

import cn.edu.zjut.dockermanager.enums.ErrorCodeEnum;
import org.slf4j.helpers.MessageFormatter;

/**
 * 业务异常类
 * 用于抛出业务逻辑中定义的异常，携带错误码、错误信息及快照信息
 */
public class BusinessException extends RuntimeException {

    private int code;  // 错误码
    private String snapshot;  // 错误快照信息，用于描述具体的错误上下文或详细信息

    /**
     * 构造函数：使用ErrorCodeEnum中的错误码和格式化的快照信息
     * @param errorCode 错误码枚举
     * @param snapshotFormat 错误快照的格式化字符串
     * @param argArray 格式化参数
     */
    public BusinessException(ErrorCodeEnum errorCode, String snapshotFormat, Object... argArray) {
        super(errorCode.getMessage());  // 设置异常的消息为错误码对应的消息
        this.code = errorCode.getCode();  // 设置错误码
        this.snapshot = MessageFormatter.arrayFormat(snapshotFormat, argArray).getMessage();  // 格式化并设置错误快照
    }

    /**
     * 构造函数：使用自定义错误码、消息和格式化的快照信息
     * @param code 错误码
     * @param message 错误消息
     * @param snapshotFormat 错误快照的格式化字符串
     * @param argArray 格式化参数
     */
    public BusinessException(int code, String message, String snapshotFormat, Object... argArray) {
        super(message);  // 设置异常的消息为自定义消息
        this.code = code;  // 设置自定义错误码
        this.snapshot = MessageFormatter.arrayFormat(snapshotFormat, argArray).getMessage();  // 格式化并设置错误快照
    }

    /**
     * 构造函数：使用ErrorCodeEnum中的错误码
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCodeEnum errorCode) {
        super(errorCode.getMessage());  // 设置异常的消息为错误码对应的消息
        this.code = errorCode.getCode();  // 设置错误码
    }

    /**
     * 构造函数：使用自定义错误消息
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);  // 设置异常的消息为自定义消息
        this.code = 1;  // 默认错误码为1
    }

    // 获取错误码
    public int getCode() {
        return code;
    }

    // 获取错误快照
    public String getSnapshot() {
        return snapshot;
    }
}
