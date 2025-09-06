package com.jonqing.usercenterbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回结果类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;

    private T data;

    private String message;

    private String description;

    /**
     * 以下构造函数，采用链式设计：即一个根构造函数，其他多个构造函数是对应根构造函数的修改
     * 这样便于维护和修改
     */
    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    // 以下的构造函数都是根构造函数的重载

    // 该三参数构造函数通过this()调用了四参数的根构造函数，第四个参数description为空字符串。
    // 这采用了链式设计，当调用方不需要传递description时，自动填充为设定的默认值
    public BaseResponse(int code, T data, String message) {
        this(code,data,message,"");
    }

    // 双参数构造函数
    public BaseResponse(int code, T data) {
        this(code,data,"","");
    }

    // 单参数构造函数
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }

}
