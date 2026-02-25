package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final NotificationRepository notificationRepo;

    public void notifyUsers(Integer senderId, Collection<Integer> recipientIds, String type, String refType,
            String refId, String title, String message) {
        for (Integer recipientId : recipientIds) {
            Notification n = new Notification();
            n.setRecipientUserId(recipientId);
            n.setType(type);
            n.setRefType(refType);
            n.setRefId(refId);
            n.setTitle(title);
            n.setMessage(message);
            notificationRepo.save(n);
        }
    }
}
