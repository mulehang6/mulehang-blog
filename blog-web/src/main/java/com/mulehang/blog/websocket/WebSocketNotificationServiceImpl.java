package com.mulehang.blog.websocket;

import com.mulehang.blog.dto.NotificationDTO;
import com.mulehang.blog.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WebSocket 通知服务实现
 * 负责通过 WebSocket 向特定用户发送实时通知
 *
 * @author mulehang
 * @since 2026-01-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final CommentNotificationHandler commentNotificationHandler;

    /**
     * 向指定用户发送通知
     *
     * @param userId 用户ID
     * @param notification 通知内容
     */
    @Override
    public void sendToUser(Long userId, NotificationDTO notification) {
        try {
            commentNotificationHandler.sendToUser(userId, notification);
            log.info("WebSocket 通知发送成功: userId={}, type={}", userId, notification.getType());
        } catch (Exception e) {
            log.error("WebSocket 通知发送失败: userId=" + userId, e);
        }
    }

    /**
     * 向所有在线用户广播通知
     *
     * @param notification 通知内容
     */
    @Override
    public void broadcast(NotificationDTO notification) {
        try {
            commentNotificationHandler.broadcast(notification);
            log.info("WebSocket 广播发送成功: type={}", notification.getType());
        } catch (Exception e) {
            log.error("WebSocket 广播发送失败", e);
        }
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数
     */
    @Override
    public int getOnlineUserCount() {
        return commentNotificationHandler.getOnlineUserCount();
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    @Override
    public boolean isUserOnline(Long userId) {
        return commentNotificationHandler.isUserOnline(userId);
    }
}
