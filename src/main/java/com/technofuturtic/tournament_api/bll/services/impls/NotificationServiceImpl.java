package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.notification.NotificationResponse;
import com.technofuturtic.tournament_api.bll.services.NotificationService;
import com.technofuturtic.tournament_api.dal.repositories.NotificationRepository;
import com.technofuturtic.tournament_api.dl.entities.*;
import com.technofuturtic.tournament_api.dl.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void notify(UserEntity recipient, NotificationType type, Integer teamId, String message) {
        NotificationEntity notification = new NotificationEntity();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTeamId(teamId);
        notification.setMessage(message);
        notificationRepository.saveAndFlush(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> findMine(Integer userId) {
        List<NotificationResponse> responses = new ArrayList<>();
        for (NotificationEntity notification : notificationRepository.findAllByRecipientIdOrderByCreatedAtDescIdDesc(userId)) {
            responses.add(NotificationResponse.fromNotification(notification));
        }
        return responses;
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Integer notificationId, Integer userId) {
        NotificationEntity notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.saveAndFlush(notification);
        }
        return NotificationResponse.fromNotification(notification);
    }
}