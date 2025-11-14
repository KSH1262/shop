package com.shop.shop.config;

import com.shop.shop.service.CustomOAuth2UserService;
import com.shop.shop.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    private final OAuthExtraInfoFilter oAuthExtraInfoFilter;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final MemberService memberService;

    public SecurityConfig(OAuthExtraInfoFilter oAuthExtraInfoFilter,
                          CustomOAuth2SuccessHandler customOAuth2SuccessHandler,
                          CustomOAuth2UserService customOAuth2UserService,
                          MemberService memberService) {
        this.oAuthExtraInfoFilter = oAuthExtraInfoFilter;
        this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.memberService = memberService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/members/**","/css/**", "/js/**", "/images/**","/item/**", "/img/**","/oauth/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/seller/**").hasRole("SELLER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                )
                .formLogin(form -> form
                        .loginPage("/members/login")
                        .loginProcessingUrl("/members/login") // 스프링 시큐리티가 해당 주소로 요청오는 로그인을 가로채서 대신 로그인 해줌
                        .defaultSuccessUrl("/")
                        .usernameParameter("email")
                        .failureUrl("/members/login/error")
                ).oauth2Login(oauth -> oauth
                        .loginPage("/members/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customOAuth2SuccessHandler)
                ).logout(logout -> logout
                        .logoutUrl("/logout")  // 로그아웃 요청 URL
                        .logoutSuccessUrl("/")  // 로그아웃 후 메인 페이지로 이동
                        .invalidateHttpSession(true)  // 세션 무효화
                        .deleteCookies("JSESSIONID")  // JSESSIONID 쿠키 삭제
                        .permitAll()
                ).exceptionHandling(exception -> exception
                        .accessDeniedPage("/error/403")
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                );
        http.addFilterAfter(oAuthExtraInfoFilter, org.springframework.security.web.context.SecurityContextHolderFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(memberService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }


}
