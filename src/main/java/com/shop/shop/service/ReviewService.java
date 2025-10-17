package com.shop.shop.service;

import com.shop.shop.dto.ReviewDto;
import com.shop.shop.entity.Item;
import com.shop.shop.entity.Member;
import com.shop.shop.entity.Review;
import com.shop.shop.repository.ItemRepository;
import com.shop.shop.repository.MemberRepository;
import com.shop.shop.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository; // 회원 정보 필요
    private final ItemRepository itemRepository; // 상품 정보 필요

    // 리뷰 등록
    public UUID saveReview(UUID itemUuid, String email, ReviewDto reviewDto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        Item item = itemRepository.findByUuid(itemUuid)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 이미 리뷰를 작성했는지 확인 (선택 사항: 1인 1리뷰 제한)
        Review existingReview = reviewRepository.findByMemberIdAndItemUuid(member.getId(), itemUuid);
        if (existingReview != null) {
            throw new IllegalStateException("이미 해당 상품에 대한 리뷰를 작성하셨습니다.");
        }

        Review review = Review.createReview(member, item, reviewDto.getRating(), reviewDto.getComment());
        reviewRepository.save(review);

        return review.getUuid();
    }

    // 특정 상품의 리뷰 목록 조회
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByItemId(UUID itemUuid) {
        List<Review> reviews = reviewRepository.findByItemUuidOrderByIdDesc(itemUuid);
        return reviews.stream()
                .map(ReviewDto::of)
                .collect(Collectors.toList());
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 리뷰 작성자와 삭제 요청자가 동일한지 확인
        if (!review.getMember().getEmail().equals(email)) {
            throw new IllegalStateException("리뷰를 삭제할 권한이 없습니다.");
        }
        reviewRepository.delete(review);
    }

}
