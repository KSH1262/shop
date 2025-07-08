package com.shop.shop.repository;

import com.shop.shop.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, QuerydslPredicateExecutor<Review> {

    // 특정 상품에 대한 모든 리뷰 조회
    List<Review> findByItemIdOrderByIdDesc(Long itemId);

    // 특정 회원이 작성한 리뷰 조회 (마이페이지 등에서 활용)
    List<Review> findByMemberIdOrderByIdDesc(Long memberId);

    // 특정 회원이 특정 상품에 대해 작성한 리뷰가 있는지 확인 (중복 리뷰 방지)
    Review findByMemberIdAndItemId(Long memberId, Long itemId);
}
