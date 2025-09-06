package com.jonqing.usercenterbackend.common;

/**
 * 返回工具类
 * 作用是以方法的形式，返回调用BaseResponse模型的返回体
 */
public class ResultUtils {
    /**
     * 封装成功返回结果
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 封装失败返回结果
     * 由于BaseResponse模型，在不同请求中接收的参数不同，所以需要多个返回方法来表示不同的返回结果
     * @param errorCode
     * @return
     */

    // 根据请求的错误码，返回对应的错误信息
    public static BaseResponse error(ErrorCode errorCode) {
        // 利用了BaseResponse的单参数构造器
        return new BaseResponse<>(errorCode);
    }

    // 封装触发自定义异常的返回结果
    // 这里的状态码参数采用完全自定义状态码，从自定义异常对象中获取
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code, null, message, description);
    }

    // 封装触发java系统运行时异常的返回结果
    // 这里的状态码参数选择了预定义错误码体系中固定的错误码
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getCode(), null, message, description);
    }

    // 多设计几种构造函数，将来用到时减少代码量
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getCode(), errorCode.getMessage(), description);
    }
}
