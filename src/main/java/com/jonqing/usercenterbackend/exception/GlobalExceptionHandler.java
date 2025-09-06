package com.jonqing.usercenterbackend.exception;

import com.jonqing.usercenterbackend.common.BaseResponse;
import com.jonqing.usercenterbackend.common.ErrorCode;
import com.jonqing.usercenterbackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 该全局异常处理器通过两个核心方法实现分层异常处理

    // 通过@ExceptionHandler注解精准捕获自定义异常BusinessException
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e) {
        // 记录自定义异常报错的详细错误信息
        log.error("businessExceptionHandler: " + e.getMessage(), e);
        // 返回自定义异常的错误码和错误信息
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
        // 前端请求失败之后，在Network控制台的响应中可以看到错误码和错误信息
    }

    // 捕获java系统运行时异常
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeExceptionHandler", e);
        // 使用固定错误码，表示系统的异常
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
        // java系统异常没有description，因此直接返回空即可
    }

}
