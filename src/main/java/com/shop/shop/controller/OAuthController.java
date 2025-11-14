package com.shop.shop.controller;

import com.shop.shop.constant.Role;
import com.shop.shop.dto.MemberFormDto;
import com.shop.shop.entity.Member;
import com.shop.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;

@Controller
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final MemberRepository memberRepository;

    // 추가 정보 입력 폼
    @GetMapping("/join-extra")
    public String showExtraForm(@AuthenticationPrincipal OAuth2User oauth2User, Model model) {

        if (oauth2User == null) {
            return "redirect:/members/login";
        }

        String email = oauth2User.getAttribute("email");
        Member member = memberRepository.findByEmail(email).orElse(null);

        // DB에도 없으면 메인으로
        if (member == null) {
            return "redirect:/";
        }

        // 이미 주소와 권한이 있다면 추가입력 페이지 접근 못하게 막기
        if (member.getAddress() != null && member.getRole() != null) {
            return "redirect:/";
        }

        // 기존 정보 DTO 에 담기
        MemberFormDto dto = new MemberFormDto();
        dto.setEmail(member.getEmail());
        dto.setName(member.getName());

        model.addAttribute("memberFormDto", dto);
        model.addAttribute("member", member);

        return "member/oauthExtraForm";
    }

    // 저장 처리
    @PostMapping("/join-extra")
    public String saveExtraInfo(MemberFormDto memberFormDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal OAuth2User oauth2User,
                                Model model) {

        if (oauth2User == null) {
            return "redirect:/members/login";
        }

        if (bindingResult.hasErrors()) {
            return "member/oauthExtraForm";
        }

        String email = oauth2User.getAttribute("email");
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 주소 병합 후 저장
        memberFormDto.mergeAddress();
        member.setAddress(memberFormDto.getAddress());

        // String → Enum
        member.setRole(Role.valueOf(memberFormDto.getRole()));

        memberRepository.save(member);

        //  로그인 세션 권한 즉시 갱신
        var authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + member.getRole().name())
        );

        OAuth2User newPrincipal = new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                "email"
        );

        Authentication newAuth =
                new OAuth2AuthenticationToken(newPrincipal, authorities, "naver");

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return "redirect:/";
    }
}
