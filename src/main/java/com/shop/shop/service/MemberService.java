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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 회원을 찾을 수 없습니다: " + email));

        // 정지된 회원이면 로그인 불가 처리
        if (member.isDeleted()) {
            throw new UsernameNotFoundException("정지된 계정입니다. 관리자에게 문의하세요.");
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().name())
                .build();
    }

    @Transactional
    public void toggleMemberStatus(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        if (member.isDeleted()) {
            member.activate();   // 정지 → 복원
        } else {
            member.deactivate(); // 정상 → 정지
        }
    }
}
