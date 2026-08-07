package com.example.controller;

import com.example.DTO.CreateNotificationRequest;
import com.example.DTO.NotificationResponse;
import com.example.model.Notification;
import com.example.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        notificationController = new NotificationController(notificationService);
    }

    private NotificationResponse response() {
        return NotificationResponse.builder()
                .id(1L)
                .userId(5L)
                .notificationType(Notification.NotificationType.BID_PLACED)
                .title("Bid Placed")
                .message("Your bid was placed")
                .read(false)
                .build();
    }

    private Notification notificationEntity(Long userId) {
        return Notification.builder().id(1L).userId(userId).build();
    }

    @Test
    void createNotification_returnsCreated() {
        CreateNotificationRequest request = new CreateNotificationRequest();
        when(notificationService.createNotification(request)).thenReturn(response());

        ResponseEntity<NotificationResponse> result = notificationController.createNotification(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getNotificationById_returnsOkForOwner() {
        when(notificationService.getNotificationEntity(1L)).thenReturn(notificationEntity(5L));
        when(notificationService.getNotificationById(1L)).thenReturn(response());

        ResponseEntity<NotificationResponse> result = notificationController.getNotificationById(1L, 5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getNotificationById_forbiddenForOtherUser() {
        when(notificationService.getNotificationEntity(1L)).thenReturn(notificationEntity(5L));

        assertThatThrownBy(() -> notificationController.getNotificationById(1L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void getMyNotifications_usesPrincipalUserId() {
        when(notificationService.getNotificationsForUser(5L)).thenReturn(List.of(response()));

        ResponseEntity<List<NotificationResponse>> result = notificationController.getMyNotifications(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        verify(notificationService).getNotificationsForUser(5L);
    }

    @Test
    void getMyUnreadNotifications_returnsOk() {
        when(notificationService.getUnreadNotificationsForUser(5L)).thenReturn(List.of(response()));

        ResponseEntity<List<NotificationResponse>> result = notificationController.getMyUnreadNotifications(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    void getMyUnreadCount_returnsOk() {
        when(notificationService.countUnreadNotificationsForUser(5L)).thenReturn(2L);

        ResponseEntity<Long> result = notificationController.getMyUnreadCount(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(2L);
    }

    @Test
    void markAsRead_returnsOkForOwner() {
        when(notificationService.getNotificationEntity(1L)).thenReturn(notificationEntity(5L));
        when(notificationService.markAsRead(1L)).thenReturn(response());

        ResponseEntity<NotificationResponse> result = notificationController.markAsRead(1L, 5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void markAsRead_forbiddenForOtherUser() {
        when(notificationService.getNotificationEntity(1L)).thenReturn(notificationEntity(5L));

        assertThatThrownBy(() -> notificationController.markAsRead(1L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void markAllAsRead_returnsOk() {
        ResponseEntity<Void> result = notificationController.markAllAsRead(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).markAllAsRead(5L);
    }

    @Test
    void deleteNotification_returnsOkForOwner() {
        when(notificationService.getNotificationEntity(1L)).thenReturn(notificationEntity(5L));

        ResponseEntity<Void> result = notificationController.deleteNotification(1L, 5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).deleteNotification(1L);
    }

    @Test
    void deleteNotification_forbiddenForOtherUser() {
        when(notificationService.getNotificationEntity(1L)).thenReturn(notificationEntity(5L));

        assertThatThrownBy(() -> notificationController.deleteNotification(1L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }
}
