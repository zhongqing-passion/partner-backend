package com.jonqing.usercenterbackend.common;

/**
 * 请求错误码
 */
public enum ErrorCode {

    // 定义五个枚举常量，每个常量包含三个请求失败相关的参数
    // 预定义错误码体系
    SUCCESS(0,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NOT_LOGIN(40100,"未登录",""),
    NO_AUTH(40101,"无权限",""),
    SYSTEM_ERROR(50000,"系统内部异常",""),
    USER_REPEAT(40002,"用户重复","");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 错误具体信息
     */
    private final String message;

    /**
     * 错误描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
