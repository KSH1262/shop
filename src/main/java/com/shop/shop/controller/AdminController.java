package com.shop.shop.controller;

import com.shop.shop.dto.AdminItemDto;
import com.shop.shop.entity.Item;
import com.shop.shop.entity.Member;
import com.shop.shop.entity.Order;
import com.shop.shop.repository.ItemRepository;
import com.shop.shop.repository.MemberRepository;
import com.shop.shop.repository.OrderRepository;
import com.shop.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final MemberService memberService;

    // 전체 주문 내역 조회
    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public String allOrders(Model model) {
        List<Order> orders = orderRepository.findAllWithOrderItemsAndItems();
        model.addAttribute("orders", orders);
        return "admin/adminOrderList";
    }

    // 전체 회원 조회 + 검색
    @GetMapping("/members")
    public String allMembers(@RequestParam(required = false) String keyword, Model model) {
        List<Member> members;

        if (keyword == null || keyword.trim().isEmpty()) {
            members = memberRepository.findAll(); // 모든 회원
        } else {
            members = memberRepository.searchMembers(keyword); // 검색
        }

        model.addAttribute("members", members);
        model.addAttribute("keyword", keyword);
        return "admin/adminMemberList";
    }

    // 전체 상품 관리
    @GetMapping("/items")
    public String allItems(Model model) {
        List<AdminItemDto> items = itemRepository.findAllItemDtos();
        model.addAttribute("items", items);
        return "admin/adminItemList";
    }

    // 회원 상태 토글 (정지 <-> 복원)
    @PostMapping("/members/{id}/toggle")
    public String toggleMemberStatus(@PathVariable Long id) {
        memberService.toggleStatus(id);
        return "redirect:/admin/members";
    }
}