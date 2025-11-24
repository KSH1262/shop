package com.shop.shop.controller;

import com.shop.shop.constant.Role;
import com.shop.shop.dto.MemberFormDto;
import com.shop.shop.entity.Member;
import com.shop.shop.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/new")
    public String memberForm(Model model){
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }


    @PostMapping("/new")
    public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors()){
            return "member/memberForm";
        }

        try {
            memberFormDto.mergeAddress();

            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (Exception e) {
            e.printStackTrace(); // 로그에 예외 출력
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginMember() {
        return "member/memberLoginForm";
    }

    @GetMapping("/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요.");
        return "member/memberLoginForm";
    }

    // 추가정보 입력 폼
    @GetMapping("/oauth/extra")
    public String oauthExtraInfoForm(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/oauthExtraForm"; // 새로 만들 html
    }

    // 추가정보 저장 처리
    @PostMapping("/oauth/extra")
    public String saveExtraInfo(@Valid MemberFormDto memberFormDto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "member/oauthExtraForm";
        }

        String email = memberFormDto.getEmail();
        Member member = memberService.findByEmail(email);

        if (member == null) {
            model.addAttribute("errorMessage", "회원 정보를 찾을 수 없습니다.");
            return "member/oauthExtraForm";
        }

        // 주소 병합 및 저장
        memberFormDto.mergeAddress();
        member.setAddress(memberFormDto.getAddress());

        // String → Enum 변환
        member.setRole(Role.valueOf(memberFormDto.getRole()));

        memberService.saveMember(member);

        return "redirect:/";
    }
}
