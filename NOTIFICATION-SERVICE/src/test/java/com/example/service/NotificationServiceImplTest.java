package com.example.service;

import com.example.DTO.CreateNotificationRequest;
import com.example.DTO.NotificationResponse;
import com.example.exception.NotificationNotFoundException;
import com.example.model.Notification;
import com.example.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository);
    }

    private CreateNotificationRequest request() {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(5L);
        request.setNotificationType(Notification.NotificationType.BID_PLACED);
        request.setTitle("Bid Placed");
        request.setMessage("Your bid was placed");
        return request;
    }

    private Notification notification(Long id, boolean read) {
        return Notification.builder()
                .id(id)
                .userId(5L)
                .notificationType(Notification.NotificationType.BID_PLACED)
                .title("Bid Placed")
                .message("Your bid was placed")
                .read(read)
                .build();
    }

    @Test
    void createNotification_success() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            return notification;
        });

        NotificationResponse response = notificationService.createNotification(request());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(5L);
        assertThat(response.getNotificationType()).isEqualTo(Notification.NotificationType.BID_PLACED);
        assertThat(response.isRead()).isFalse();
    }

    @Test
    void getNotificationById_success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification(1L, false)));

        NotificationResponse response = notificationService.getNotificationById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Bid Placed");
    }

    @Test
    void getNotificationById_notFound_throws() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(1L))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getNotificationsForUser_returnsList() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(notification(1L, false), notification(2L, true)));

        List<NotificationResponse> result = notificationService.getNotificationsForUser(5L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(NotificationResponse::getUserId).containsOnly(5L);
    }

    @Test
    void getUnreadNotificationsForUser_returnsUnreadOnly() {
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(5L)).thenReturn(List.of(notification(1L, false)));

        List<NotificationResponse> result = notificationService.getUnreadNotificationsForUser(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isRead()).isFalse();
    }

    @Test
    void countUnreadNotificationsForUser_delegates() {
        when(notificationRepository.countByUserIdAndReadFalse(5L)).thenReturn(3L);

        assertThat(notificationService.countUnreadNotificationsForUser(5L)).isEqualTo(3L);
    }

    @Test
    void markAsRead_success() {
        Notification notification = notification(1L, false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1L);

        assertThat(response.isRead()).isTrue();
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void markAsRead_notFound_throws() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void markAllAsRead_success() {
        Notification n1 = notification(1L, false);
        Notification n2 = notification(2L, false);
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(5L)).thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(5L);

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();
        verify(notificationRepository).saveAll(List.of(n1, n2));
    }

    @Test
    void markAllAsRead_noUnread_savesEmpty() {
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(5L)).thenReturn(List.of());

        notificationService.markAllAsRead(5L);

        verify(notificationRepository).saveAll(List.of());
    }

    @Test
    void deleteNotification_success() {
        when(notificationRepository.existsById(1L)).thenReturn(true);

        notificationService.deleteNotification(1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void deleteNotification_notFound_throws() {
        when(notificationRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> notificationService.deleteNotification(1L))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository, never()).deleteById(any());
    }
}
