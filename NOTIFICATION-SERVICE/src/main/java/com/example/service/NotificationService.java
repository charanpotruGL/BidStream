package com.example.service;

import com.example.DTO.CreateNotificationRequest;
import com.example.DTO.NotificationResponse;
import com.example.model.Notification;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotificationById(Long id);

    List<NotificationResponse> getNotificationsForUser(Long userId);

    List<NotificationResponse> getUnreadNotificationsForUser(Long userId);

    long countUnreadNotificationsForUser(Long userId);

    NotificationResponse markAsRead(Long id);

    void markAllAsRead(Long userId);

    void deleteNotification(Long id);

    Notification getNotificationEntity(Long id);
}
