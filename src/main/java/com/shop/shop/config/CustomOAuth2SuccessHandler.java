package com.shop.shop.config;

import com.shop.shop.entity.Member;
import com.shop.shop.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            email = (String) oauth2User.getAttribute("email");
        } else {
            email = authentication.getName();
        }

        if (email != null) {
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null && (member.getAddress() == null || member.getRole() == null)) {
                // 추가정보가 필요하면 폼 페이지로 이동
                getRedirectStrategy().sendRedirect(request, response, "/oauth/join-extra");
                return;
            }
        }

        // 기본 성공 리다이렉트
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
