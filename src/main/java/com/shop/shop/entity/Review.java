package com.shop.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;


@Entity
@Table(name="review")
@Getter
@Setter
@ToString
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "review_uuid", columnDefinition = "uuid", unique = true, nullable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 리뷰 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // 리뷰 대상 상품

    private int rating; // 평점 (1~5점)

    @Lob
    private String comment; // 리뷰 내용

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    public static Review createReview(Member member, Item item, int rating, String comment) {
        Review review = new Review();
        review.setMember(member);
        review.setItem(item);
        review.setRating(rating);
        review.setComment(comment);
        return review;
    }
}
