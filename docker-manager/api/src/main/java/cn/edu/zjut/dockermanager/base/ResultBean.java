package cn.edu.zjut.dockermanager.base;

import cn.edu.zjut.dockermanager.enums.ErrorCodeEnum;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


// 该类用于封装API响应数据，包括消息、状态码和返回的数据
@Data
@Accessors(chain = true)  // 开启链式调用setter方法的支持
public class ResultBean<T> implements Serializable {  // 泛型类ResultBean，用于包裹不同类型的数据
    private static final long serialVersionUID = 1L;  // 序列化版本UID，确保版本兼容性
    private String msg = "success";  // 默认为“success”，表示请求成功
    private int code = 0;  // 默认为0，表示成功状态码
    private T data;  // 返回的数据，可以是任何类型

    // 无参构造方法
    public ResultBean() {
    }

    // 带有状态码和消息的构造方法
    public ResultBean(int code, String message) {
        this.code = code;
        this.msg = message;
    }

    // 带有数据的构造方法
    public ResultBean(T data) {
        this.data = data;
    }

    // 静态方法，封装成功的响应，传入数据
    public static <T> ResultBean success(T data) {
        return new ResultBean<T>().setData(data);  // 返回封装了数据的ResultBean对象
    }

    // 静态方法，封装失败的响应，传入错误码枚举
    public static ResultBean error(ErrorCodeEnum errorCode) {
        return new ResultBean(errorCode.getCode(), errorCode.getMessage());  // 返回封装了错误信息的ResultBean对象
    }

    // 静态方法，封装失败的响应，传入自定义的错误消息
    public static <T> ResultBean error(String msg) {
        return new ResultBean<>().setCode(1).setMsg(msg);  // 返回状态码为1、消息为传入的msg的ResultBean对象
    }

    // 将ResultBean对象转为JSON字符串
    public String toJSONString() {
        return JSON.toJSON(this).toString();  // 使用Fastjson将ResultBean对象转为JSON格式的字符串
    }
}