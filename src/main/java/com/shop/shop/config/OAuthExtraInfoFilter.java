package com.shop.shop.config;

import com.shop.shop.entity.Member;
import com.shop.shop.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthExtraInfoFilter extends OncePerRequestFilter {

    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 추가 정보 입력 페이지 자체는 허용
        if (uri.startsWith("/oauth/join-extra") || uri.startsWith("/css") ||
                uri.startsWith("/js") || uri.startsWith("/images") || uri.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() &&
                auth.getPrincipal() instanceof UserDetails userDetails) {

            String email = userDetails.getUsername();
            Member member = memberRepository.findByEmail(email).orElse(null);

            if (member != null && (member.getAddress() == null || member.getRole() == null)) {
                response.sendRedirect("/oauth/join-extra");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
