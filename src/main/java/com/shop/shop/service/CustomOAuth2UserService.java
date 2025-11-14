package com.shop.shop.service;

import com.shop.shop.constant.Role;
import com.shop.shop.entity.Member;
import com.shop.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        if (response == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"),
                    "Cannot get user info from Naver");
        }

        String email = (String) response.get("email");
        String name = (String) response.get("name");
        String id = (String) response.get("id");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"),
                    "Email not provided by Naver. Please sign up manually.");
        }

        // DB에서 기존 사용자 찾기 — 없으면 최소 정보로 생성(주소/role은 비워둠)
        Member member = memberRepository.findByEmail(email).orElseGet(() -> {
            Member newMember = new Member();
            newMember.setEmail(email);
            newMember.setName(name != null ? name : ("naver_" + id));
            newMember.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 내부 랜덤 비번
            newMember.setRole(null);       // 중요: 추가 정보 입력 필요 표시 (null)
            newMember.setAddress(null);    // 주소도 없음
            return memberRepository.save(newMember);
        });

        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(member.getRole() != null ? member.getRole().name() : "ROLE_USER")
        );

        // attributes로 response(네이버 필드들) 사용, 이름키는 email로 세팅
        return new DefaultOAuth2User(authorities, response, "email");
    }
}
