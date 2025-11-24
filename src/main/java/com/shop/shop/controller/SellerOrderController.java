package com.shop.shop.controller;

import com.shop.shop.dto.SellerOrderDto;
import com.shop.shop.entity.Order;
import com.shop.shop.repository.OrderRepository;
import com.shop.shop.service.OrderService;
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

    private final OrderService orderService;

    @GetMapping
    public String sellerOrders(Model model, Principal principal) {
        String sellerEmail = principal.getName();
        List<SellerOrderDto> orders = orderService.getSellerOrders(sellerEmail);

        model.addAttribute("orders", orders);
        return "seller/sellerOrderList";
    }
}
