package dev.handsup.comment.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.handsup.auction.domain.Auction;
import dev.handsup.auction.service.AuctionService;
import dev.handsup.comment.domain.Comment;
import dev.handsup.comment.dto.request.RegisterCommentRequest;
import dev.handsup.comment.dto.response.CommentResponse;
import dev.handsup.comment.mapper.CommentMapper;
import dev.handsup.comment.repository.CommentRepository;
import dev.handsup.common.dto.CommonMapper;
import dev.handsup.common.dto.PageResponse;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.service.FCMService;
import dev.handsup.user.domain.User;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final AuctionService auctionService;
	private final CommentRepository commentRepository;
	private final FCMService fcmService;

	@Transactional
	public CommentResponse registerAuctionComment(Long auctionId, RegisterCommentRequest request, User user) {
		Auction auction = auctionService.getAuctionById(auctionId);
		auction.validateIfCommentAvailable();
		Comment comment = commentRepository.save(CommentMapper.toComment(request, auction, user));

		fcmService.sendMessage(
			user.getEmail(),
			user.getNickname(),
			auction.getSeller().getEmail(),
			NotificationType.COMMENT,
			auctionId
		);

		return CommentMapper.toCommentResponse(comment);
	}

	@Transactional(readOnly = true)
	public PageResponse<CommentResponse> getAuctionComments(Long auctionId, Pageable pageable) {
		Auction auction = auctionService.getAuctionById(auctionId);

		Slice<CommentResponse> responsePage = commentRepository
			.findByAuctionOrderByCreatedAtDesc(auction, pageable)
			.map(CommentMapper::toCommentResponse);

		return CommonMapper.toPageResponse(responsePage);
	}
}
