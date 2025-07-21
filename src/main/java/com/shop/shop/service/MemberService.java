package com.shop.shop.service;

import com.shop.shop.entity.Member;
import com.shop.shop.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional // 에러 발생시 콜백
@RequiredArgsConstructor // final, @NotNull 붙은 필드에 생성자 생성
public class MemberService implements UserDetailsService { // MemberService 가 UserDetailsService 구현

    private final MemberRepository memberRepository;

    public Member saveMember(Member member){
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    private void validateDuplicateMember(Member member) { // 가입된 회원의 경우 예외 발생
        Optional<Member> findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember.isPresent()){ // 해당 이메일을 가진 회원이 이미 존재한다면
            throw new IllegalStateException("이미 가입된 회원입니다."); // 중복 회원이므로 예외 발생
        }
    }

    @Override
    // 로그인할 유저의 email을 전달 받음
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 이메일의 회원을 찾을 수 없습니다."));

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
