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

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/item/{itemId}/reviews")
    public @ResponseBody ResponseEntity<List<ReviewDto>> getReviews(@PathVariable("itemId") Long itemId) {
        List<ReviewDto> reviews = reviewService.getReviewsByItemId(itemId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    // 리뷰 등록 (POST 요청)
    @PostMapping("/review/{itemId}")
    public @ResponseBody ResponseEntity<String> createReview(@PathVariable("itemId") Long itemId,
                                                             @RequestBody @Valid ReviewDto reviewDto,
                                                             BindingResult bindingResult,
                                                             Principal principal) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> sb.append(error.getDefaultMessage()).append("\n"));
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        try {
            String email = principal.getName(); // 현재 로그인한 사용자 이메일
            reviewService.saveReview(itemId, email, reviewDto);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); // 중복 리뷰 등
        } catch (Exception e) {
            return new ResponseEntity<>("리뷰 등록 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("리뷰가 성공적으로 등록되었습니다.", HttpStatus.OK);
    }

    // 리뷰 삭제 (DELETE 요청)
    @DeleteMapping("/review/{reviewId}")
    public @ResponseBody ResponseEntity<String> deleteReview(@PathVariable("reviewId") Long reviewId,
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
