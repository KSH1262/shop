package com.shop.shop.service;

import com.shop.shop.entity.Member;
import com.shop.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // 에러 발생시 콜백
@RequiredArgsConstructor // final, @NotNull 붙은 필드에 생성자 생성
public class MemberService implements UserDetailsService { // MemberService 가 UserDetailsService 구현

    private final MemberRepository memberRepository;

    public Member saveMember(Member member){
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member) { // 가입된 회원의 경우 예외 발생
            Member findMember = memberRepository.findByEmail(member.getEmail());
            if (findMember != null){
                throw new IllegalStateException("이미 가입된 회원입니다.");
            }
    }

    @Override
    // 로그인할 유저의 email을 전달 받음
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if (member == null){
            throw new UsernameNotFoundException(email);
        }

        return User.builder() // User 객체를 생성하기 위해 email, 비밀번호, role을 파라미터로 넘겨줌
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }
}
