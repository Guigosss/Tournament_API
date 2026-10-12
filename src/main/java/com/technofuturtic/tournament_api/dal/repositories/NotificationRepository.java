package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {
    List<NotificationEntity> findAllByRecipientIdOrderByCreatedAtDescIdDesc(Integer recipientId);
    Optional<NotificationEntity> findByIdAndRecipientId(Integer id, Integer recipientId);
}