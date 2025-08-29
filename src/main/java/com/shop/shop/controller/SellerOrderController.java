package com.shop.shop.controller;

import com.shop.shop.dto.SellerOrderDto;
import com.shop.shop.entity.Order;
import com.shop.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/seller/orders")
public class SellerOrderController {

    private final OrderRepository orderRepository;

    @GetMapping
    public String sellerOrders(Model model, Principal principal) {
        String sellerEmail = principal.getName();

        // 엔티티 대신 DTO 직접 조회
        List<SellerOrderDto> orders = orderRepository.findSellerOrders(sellerEmail);

        model.addAttribute("orders", orders);
        return "order/sellerOrderList";
    }
}
