package dev.handsup.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import dev.handsup.common.exception.ValidationException;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.dto.request.SaveFCMTokenRequest;
import dev.handsup.notification.repository.FCMTokenRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class FCMService {

    private final FCMTokenRepository fcmTokenRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final NotificationService notificationService;

    public void sendMessage(
        String senderEmail,
        String senderNickname,
        String receiverEmail,
        NotificationType notificationType,
        Long auctionId
    ) {
        String fcmToken = fcmTokenRepository.getFcmToken(receiverEmail);
        if (fcmToken == null) {
            return;
        }

        if (!notificationType.includeSenderNickname()) {
            senderNickname = "";
        }

        Message message = Message.builder()
            .setNotification(Notification.builder()
                .setTitle(notificationType.getTitle())
                .setBody(senderNickname + notificationType.getContent())
                .build())
            .setToken(fcmToken)
            .build();

        try {
            firebaseMessaging.send(message);
            log.info(
                "[SEND_MESSAGE_SUCCESS] senderEmail: {}, senderNickname: {}, receiverEmail: {}, notificationType: {}, auctionId: {}",
                senderEmail, senderNickname, receiverEmail, notificationType.name(), auctionId
            );
        } catch (FirebaseMessagingException e) {
            log.info(
                "[SEND_MESSAGE_FAILED] reason: {}, senderEmail: {}, senderNickname: {}, receiverEmail: {}, notificationType: {}, auctionId: {}",
                e.getMessage(), senderEmail, senderNickname, receiverEmail, notificationType.name(), auctionId
            );
            throw new ValidationException(e.getMessage());
        }

        notificationService.saveNotification(
            senderEmail,
            receiverEmail,
            senderNickname + notificationType.getContent(),
            notificationType,
            auctionId
        );
    }

    public void saveFcmToken(String userEmail, SaveFCMTokenRequest request) {
        fcmTokenRepository.saveFcmToken(userEmail, request.fcmToken());
    }

    public void deleteFcmToken(String email) {
        fcmTokenRepository.deleteFcmToken(email);
    }
}
