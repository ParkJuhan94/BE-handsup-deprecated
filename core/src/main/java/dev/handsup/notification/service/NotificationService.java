package dev.handsup.notification.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.handsup.auction.domain.Auction;
import dev.handsup.auction.service.AuctionService;
import dev.handsup.common.dto.CommonMapper;
import dev.handsup.common.dto.PageResponse;
import dev.handsup.notification.domain.Notification;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.dto.NotificationMapper;
import dev.handsup.notification.dto.response.NotificationResponse;
import dev.handsup.notification.repository.NotificationRepository;
import dev.handsup.user.domain.User;
import dev.handsup.user.service.UserService;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final AuctionService auctionService;

    public long countNotificationsByUserEmail(String userEmail) {
        return notificationRepository.countByReceiverEmail(userEmail);
    }

    @Transactional
    public void saveNotification(
        String senderEmail,
        String receiverEmail,
        String content,
        NotificationType notificationType,
        Long auctionId
    ) {
        Auction auction = auctionService.getAuctionById(auctionId);
        Notification notification = Notification.of(senderEmail, receiverEmail, content, notificationType, auction);
        notificationRepository.save(notification);
    }

    @Transactional
    public PageResponse<NotificationResponse> getNotifications(User user, Pageable pageable) {
        Slice<NotificationResponse> notificationResponsePage = notificationRepository
            .findByReceiverEmailOrderByCreatedAtDesc(user.getEmail(), pageable)
            .map(notification -> {
                String senderEmail = notification.getSenderEmail();
                String senderProfileImageUrl = userService.getUserByEmail(senderEmail).getProfileImageUrl();
                Auction auction = notification.getAuction();

                return NotificationMapper.toNotificationResponse(
                    notification.getId(),
                    notification.getType(),
                    notification.getContent(),
                    senderProfileImageUrl,
                    auction.getId(),
                    auction.getProduct().getImages().get(0).getImageUrl()
                );
            });

        // 사용자의 읽은 알림 수 갱신
        userService.updateReadNotificationCount(user, notificationResponsePage.getContent().size());

        return CommonMapper.toPageResponse(notificationResponsePage);
    }
}
