package com.shop.shop.repository;

import com.shop.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email); // 이메일 중복 검사 위해 쿼리 메소드 작성
}
