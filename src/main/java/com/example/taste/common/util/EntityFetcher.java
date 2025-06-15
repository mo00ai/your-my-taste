package com.example.taste.common.util;

import static com.example.taste.domain.board.exception.BoardErrorCode.BOARD_NOT_FOUND;
import static com.example.taste.domain.event.exception.EventErrorCode.NOT_FOUND_EVENT;
import static com.example.taste.domain.favor.exception.FavorErrorCode.NOT_FOUND_FAVOR;
import static com.example.taste.domain.image.exception.ImageErrorCode.IMAGE_NOT_FOUND;
import static com.example.taste.domain.match.exception.MatchErrorCode.USER_MATCH_INFO_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_NOT_FOUND;
import static com.example.taste.domain.pk.exception.PkErrorCode.PK_CRITERIA_NOT_FOUND;
import static com.example.taste.domain.store.exception.StoreErrorCode.BUCKET_NOT_FOUND;
import static com.example.taste.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;
import static com.example.taste.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.comment.entity.Comment;
import com.example.taste.domain.comment.exception.CommentErrorCode;
import com.example.taste.domain.comment.repository.CommentRepository;
import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.event.repository.EventRepository;
import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.repository.ImageRepository;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.repository.UserMatchInfoRepository;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.pk.entity.PkCriteria;
import com.example.taste.domain.pk.repository.PkCriteriaRepository;
import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.review.exception.ReviewErrorCode;
import com.example.taste.domain.review.repository.ReviewRepository;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class EntityFetcher {
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final ReviewRepository reviewRepository;
	private final PkCriteriaRepository pkCriteriaRepository;
	private final PartyRepository partyRepository;
	private final PartyInvitationRepository partyInvitationRepository;
	private final ImageRepository imageRepository;
	private final FavorRepository favorRepository;
	private final EventRepository eventRepository;
	private final CommentRepository commentRepository;
	private final BoardRepository boardRepository;
	private final UserMatchInfoRepository userMatchInfoRepository;
	private final StoreBucketRepository storeBucketRepository;

	public User getUserOrThrow(Long id) {
		return userRepository.findById(id)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
	}

	public User getUndeletedUserOrThrow(Long id) {
		return userRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
	}

	public Store getStoreOrThrow(Long id) {
		return storeRepository.findById(id)
			.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
	}

	public StoreBucket getStoreBucketOrThrow(Long id) {
		return storeBucketRepository.findById(id)
			.orElseThrow(() -> new CustomException(BUCKET_NOT_FOUND));
	}

	public Review getReviewOrThrow(Long id) {
		return reviewRepository.findById(id)
			.orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));
	}

	public PkCriteria getPkCriteriaOrThrow(Long id) {
		return pkCriteriaRepository.findById(id)
			.orElseThrow(() -> new CustomException(PK_CRITERIA_NOT_FOUND));
	}

	public Party getPartyOrThrow(Long id) {
		return partyRepository.findById(id)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
	}

	public PartyInvitation getPartyInvitationOrThrow(Long id) {
		return partyInvitationRepository.findById(id)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));
	}

	public Image getImageOrThrow(Long id) {
		return imageRepository.findById(id)
			.orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));
	}

	public Favor getFavorOrThrow(Long id) {
		return favorRepository.findById(id).orElseThrow(
			() -> new CustomException(NOT_FOUND_FAVOR));
	}

	public Event getEventOrThrow(Long id) {
		return eventRepository.findById(id)
			.orElseThrow(() -> new CustomException(NOT_FOUND_EVENT));
	}

	public Comment getCommentOrThrow(Long id) {
		return commentRepository.findById(id)
			.orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));
	}

	public Board getBoardOrThrow(Long id) {
		return boardRepository.findById(id)
			.orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));
	}

	public UserMatchInfo getUserMatchInfoOrThrow(Long id) {
		return userMatchInfoRepository.findById(id)
			.orElseThrow(() -> new CustomException(USER_MATCH_INFO_NOT_FOUND));
	}
}
