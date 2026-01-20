package com.mulehang.blog.controller;

import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息控制器
 */
@Tag(name = "用户管理", description = "用户信息相关接口")
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息（需要认证）")
    @GetMapping("/current")
    public Result<UserInfoVO> getCurrentUser() {
        UserInfoVO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(currentUser);
    }
}

