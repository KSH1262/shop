package com.shop.shop.controller;

import com.shop.shop.dto.OrderDto;
import com.shop.shop.dto.OrderHistDto;
import com.shop.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    // @RequestBody : HTTP 요청의 본문 body에 담긴 내용을 자바 객체로 전달
    // @ResponseBody : 자바 객체를 HTTP 요청의 body로 전달
    public @ResponseBody ResponseEntity order (@RequestBody @Valid OrderDto orderDto,
                                               BindingResult bindingResult, Principal principal){

        if (bindingResult.hasErrors()){ // orderDto 객체에 데이터 바인딩 시 에러 있는지 확인
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors){
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST); // 에러 정보 반환
        }

        String email = principal.getName(); // principal 객체에서 현재 로그인한 회원의 이메일 조회
        Long orderId;
        try {
            orderId = orderService.order(orderDto, email); // 주문 로직 호출
        } catch (Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(orderId, HttpStatus.OK); // 요청 성공 HTTP 응답 상태 코드 반환
    }

    @GetMapping({"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page")Optional<Integer> page, Principal principal, Model model){
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 4); // 한 번에 가지고 올 주문 개수: 4

        // 이메일, 페이징 객체 파라미터로 전달, 주문 목록 데이터를 리턴 값으로
        Page<OrderHistDto> ordersHistDtoList = orderService.getOrderList(principal.getName(), pageable);

        model.addAttribute("orders", ordersHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);

        return "order/orderHist";
    }

    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity cancelOrder(@PathVariable("orderId") Long orderId, Principal principal){

        if (!orderService.validateOrder(orderId, principal.getName())) { // 주문 취소 권한 검사
            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        orderService.cancelOrder(orderId); // 주문 취소 로직 호출
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }
}
