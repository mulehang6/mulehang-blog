package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.dto.GuestLoginRequest;
import com.mulehang.blog.dto.LoginRequest;
import com.mulehang.blog.dto.RegisterRequest;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.entity.SysUserRole;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.mapper.SysUserRoleMapper;
import com.mulehang.blog.service.AuthService;
import com.mulehang.blog.util.JwtUtil;
import com.mulehang.blog.util.PasswordUtil;
import com.mulehang.blog.vo.LoginResponse;
import com.mulehang.blog.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 验证密码
        if (!passwordUtil.matches(request.getPassword(), user.getPasswordSalt(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 4. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 5. 查询用户角色
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());

        // 6. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);

        // 7. 构建响应
        UserInfoVO userInfo = UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .profile(user.getProfile())
                .roles(roles)
                .lastLoginTime(user.getLastLoginTime())
                .build();

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userInfo(userInfo)
                .build();
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 登录响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, request.getEmail()));
            if (count > 0) {
                throw new RuntimeException("邮箱已被注册");
            }
        }

        // 3. 生成盐并加密密码
        String salt = passwordUtil.generateSalt();
        String passwordHash = passwordUtil.encryptPassword(request.getPassword(), salt);

        // 4. 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordHash);
        user.setPasswordSalt(salt);
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        userMapper.insert(user);

        // 5. 分配默认角色（USER）
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2L); // 假设角色ID 2 为普通用户
        userRoleMapper.insert(userRole);

        // 6. 自动登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(request.getUsername());
        loginRequest.setPassword(request.getPassword());
        return login(loginRequest);
    }

    /**
     * 退出登录
     *
     * @param userId 用户 ID
     */
    @Override
    public void logout(Long userId) {
        // JWT 是无状态的，这里可以用于清理缓存等操作
        log.info("用户 {} 退出登录", userId);
    }

    /**
     * 访客登录（生成临时访问令牌，用于测试）
     *
     * @param request 访客登录请求
     * @return 登录响应
     */
    @Override
    public LoginResponse guestLogin(GuestLoginRequest request) {
        // 1. 生成访客标识
        String guestId = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        String nickname = request.getNickname() != null && !request.getNickname().isEmpty()
                ? request.getNickname()
                : "访客_" + guestId.substring(6);

        // 2. 构建访客用户信息（不存储到数据库）
        UserInfoVO guestInfo = UserInfoVO.builder()
                .id(-1L) // 使用负数 ID 表示访客
                .username(guestId)
                .nickname(nickname)
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + guestId)
                .profile("临时访客用户")
                .roles(Collections.singletonList("GUEST"))
                .build();

        // 3. 生成临时 JWT Token（使用自定义过期时间）
        Long expiresIn = request.getExpiresIn() != null ? request.getExpiresIn() : 3600L;
        String token = jwtUtil.generateToken(-1L, guestId, Collections.singletonList("GUEST"), expiresIn);

        // 4. 构建响应
        log.info("访客登录成功: {}, 昵称: {}, 有效期: {}秒", guestId, nickname, expiresIn);
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userInfo(guestInfo)
                .build();
    }
}

