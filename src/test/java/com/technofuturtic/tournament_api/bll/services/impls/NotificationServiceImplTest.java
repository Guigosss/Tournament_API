package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.dal.repositories.NotificationRepository;
import com.technofuturtic.tournament_api.dl.entities.NotificationEntity;
import com.technofuturtic.tournament_api.dl.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.technofuturtic.tournament_api.bll.services.impls.ServiceTestData.*;

class NotificationServiceImplTest {
    private final NotificationRepository notifications = mock(NotificationRepository.class);
    private final NotificationServiceImpl service = new NotificationServiceImpl(notifications);

    private NotificationEntity notification() {
        var notification = new NotificationEntity();
        notification.setId(8);
        notification.setRecipient(player(2));
        notification.setType(NotificationType.MEMBER_LEFT);
        notification.setTeamId(7);
        notification.setMessage("Départ du joueur");
        return notification;
    }

    @Test void notifyPersistsRecipientTypeTeamAndMessage() {
        var recipient = player(2);
        service.notify(recipient, NotificationType.MEMBER_REMOVED, 7, "Retrait");
        var captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notifications).saveAndFlush(captor.capture());
        var saved = captor.getValue();
        assertSame(recipient, saved.getRecipient());
        assertEquals(NotificationType.MEMBER_REMOVED, saved.getType());
        assertEquals(7, saved.getTeamId());
        assertEquals("Retrait", saved.getMessage());
        assertNull(saved.getReadAt());
    }

    @Test void findMineUsesRecipientFilterAndMapsResponse() {
        when(notifications.findAllByRecipientIdOrderByCreatedAtDescIdDesc(2)).thenReturn(List.of(notification()));
        var result = service.findMine(2);
        assertEquals(1, result.size());
        assertEquals(8, result.get(0).id());
        assertEquals("Départ du joueur", result.get(0).message());
        verify(notifications).findAllByRecipientIdOrderByCreatedAtDescIdDesc(2);
    }

    @Test void markReadSetsDateAndDoesNotDeleteHistory() {
        var notification = notification();
        when(notifications.findByIdAndRecipientId(8, 2)).thenReturn(Optional.of(notification));
        service.markRead(8, 2);
        assertNotNull(notification.getReadAt());
        verify(notifications).saveAndFlush(notification);
        verify(notifications, never()).delete(any());
    }

    @Test void markReadIsIdempotent() {
        var notification = notification();
        var date = LocalDateTime.of(2026, 1, 1, 12, 0);
        notification.setReadAt(date);
        when(notifications.findByIdAndRecipientId(8, 2)).thenReturn(Optional.of(notification));
        service.markRead(8, 2);
        assertEquals(date, notification.getReadAt());
        verify(notifications, never()).saveAndFlush(any());
    }

    @Test void anotherRecipientOrMissingNotificationReturns404() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.markRead(8, 99)).getStatusCode().value());
        verify(notifications).findByIdAndRecipientId(8, 99);
        verify(notifications, never()).saveAndFlush(any());
    }
}
