package com.shop.shop.controller;

import com.shop.shop.dto.AdminItemDto;
import com.shop.shop.entity.Item;
import com.shop.shop.entity.Member;
import com.shop.shop.entity.Order;
import com.shop.shop.repository.ItemRepository;
import com.shop.shop.repository.MemberRepository;
import com.shop.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    // 전체 주문 내역 조회
    @GetMapping("/orders")
    @Transactional(readOnly = true) // 트랜잭션 어노테이션 추가
    public String allOrders(Model model) {
        List<Order> orders = orderRepository.findAllWithOrderItemsAndItems(); // 수정된 메서드 호출
        model.addAttribute("orders", orders);
        return "admin/adminOrderList";
    }

    // 전체 회원 조회
    @GetMapping("/members")
    public String allMembers(Model model) {
        List<Member> members = memberRepository.findAll();
        model.addAttribute("members", members);
        return "admin/adminMemberList";
    }

    // 전체 상품 관리
    @GetMapping("/items")
    public String allItems(Model model) {
        List<AdminItemDto> items = itemRepository.findAllItemDtos();
        model.addAttribute("items", items);
        return "admin/adminItemList";
    }
}