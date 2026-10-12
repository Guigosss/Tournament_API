package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.notification.NotificationResponse;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import com.technofuturtic.tournament_api.dl.enums.NotificationType;
import java.util.List;

public interface NotificationService {
    void notify(UserEntity recipient, NotificationType type, Integer teamId, String message);
    List<NotificationResponse> findMine(Integer userId);
    NotificationResponse markRead(Integer notificationId, Integer userId);
}