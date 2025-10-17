package com.shop.shop.controller;

import com.shop.shop.dto.ReviewDto;
import com.shop.shop.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/item/{uuid}/reviews")
    @ResponseBody
    public ResponseEntity<List<ReviewDto>> getReviews(@PathVariable("uuid") UUID itemUuid) {
        List<ReviewDto> reviews = reviewService.getReviewsByItemId(itemUuid);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @PostMapping("/review/{uuid}")
    @ResponseBody
    public ResponseEntity<String> createReview(@PathVariable("uuid") UUID itemUuid,
                                               @RequestBody @Valid ReviewDto reviewDto,
                                               BindingResult bindingResult,
                                               Principal principal) {
        // 입력값 검증
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors()
                    .forEach(error -> sb.append(error.getDefaultMessage()).append("\n"));
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        try {
            String email = principal.getName(); // 로그인 사용자 이메일
            reviewService.saveReview(itemUuid, email, reviewDto); // UUID 기반 저장
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); // 중복 리뷰 등
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("리뷰 등록 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("리뷰가 성공적으로 등록되었습니다.", HttpStatus.OK);
    }

    @DeleteMapping("/review/{reviewId}")
    @ResponseBody
    public ResponseEntity<String> deleteReview(@PathVariable("reviewId") Long reviewId,
                                               Principal principal) {
        try {
            String email = principal.getName();
            reviewService.deleteReview(reviewId, email);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN); // 권한 없음
        } catch (Exception e) {
            return new ResponseEntity<>("리뷰 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("리뷰가 성공적으로 삭제되었습니다.", HttpStatus.OK);
    }
}
