package com.shop.shop.repository;

import com.shop.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email); // 이메일 중복 검사 위해 쿼리 메소드 작성

    // 검색용 (이메일, 이름)
    @Query("select m from Member m where " +
            "( :keyword is null or m.email like %:keyword% or m.name like %:keyword%) " +
            "and m.isDeleted = false")
    List<Member> searchMembers(@Param("keyword") String keyword);
}
