package com.mulehang.blog.exception;

import com.mulehang.blog.model.Result;
import com.mulehang.blog.model.ResultCodeEnum;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理（统一返回 {@link com.mulehang.blog.model.Result}）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理非法参数异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.fail(ResultCodeEnum.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理方法参数校验异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().isEmpty()
                ? "参数校验失败"
                : e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return Result.fail(ResultCodeEnum.BAD_REQUEST, msg);
    }

    /**
     * 处理约束违反异常。
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        return Result.fail(ResultCodeEnum.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理通用异常。
     *
     * <p>生产环境可在这里接入日志与告警。</p>
     *
     * @param ignored 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ignored) {
        return Result.fail(ResultCodeEnum.INTERNAL_ERROR);
    }
}
