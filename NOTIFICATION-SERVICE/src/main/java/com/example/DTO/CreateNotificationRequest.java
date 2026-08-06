package com.example.DTO;

import com.example.model.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Notification type is required")
    private Notification.NotificationType notificationType;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 1000, message = "Message must be at most 1000 characters")
    private String message;
}
