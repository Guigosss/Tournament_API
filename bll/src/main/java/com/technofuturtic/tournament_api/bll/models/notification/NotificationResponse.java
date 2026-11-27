package com.technofuturtic.tournament_api.bll.models.notification;

import com.technofuturtic.tournament_api.dl.entities.NotificationEntity;
import com.technofuturtic.tournament_api.dl.enums.NotificationType;
import java.time.LocalDateTime;


public record NotificationResponse(
        Integer id,
        NotificationType type,
        Integer teamId,
        Integer invitationId,
        String message,
        LocalDateTime createdAt,
        boolean read,
        LocalDateTime readAt
) {
    public static NotificationResponse fromNotification(NotificationEntity notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTeamId(),
                notification.getInvitationId(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getReadAt() != null,
                notification.getReadAt()
        );
    }
}
