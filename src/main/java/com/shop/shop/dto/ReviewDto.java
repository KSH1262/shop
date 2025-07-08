package com.shop.shop.dto;

import com.shop.shop.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewDto {

    private Long id;

    @NotNull(message = "평점은 필수 입력 값입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Integer rating;

    @NotBlank(message = "리뷰 내용은 필수 입력 값입니다.")
    private String comment;

    private String memberName; // 리뷰 작성자 이름
    private String memberEmail; // 리뷰 작성자 이메일
    private LocalDateTime regTime; // 작성 시간

    private static ModelMapper modelMapper = new ModelMapper(); // ModelMapper 인스턴스

    public static ReviewDto of(Review review) {
        ReviewDto reviewDto = modelMapper.map(review, ReviewDto.class);
        reviewDto.setMemberName(review.getMember().getName()); // 회원 이름 설정
        reviewDto.setMemberEmail(review.getMember().getEmail()); // 회원 이메일 설정
        return reviewDto;
    }
}
