package com.shop.shop.entity;

import com.shop.shop.constant.Role;
import com.shop.shop.converter.AddressEncryptConverter;
import com.shop.shop.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "member")
@Getter
@Setter
@ToString
public class Member extends BaseEntity{

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Column(unique = true) // 이메일 unique
    private String email;

    private String password;

    @Convert(converter = AddressEncryptConverter.class)
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean isDeleted = false;  // 탈퇴 여부

    public void deactivate() {
        this.isDeleted = true;
    }

    public void activate() {
        this.isDeleted = false; // 회원 복원
    }

    public static Member createMember(MemberFormDto memberFormDto,
                                      PasswordEncoder passwordEncoder){ // member 엔티티 생성
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        String password = passwordEncoder.encode(memberFormDto.getPassword()); // 비밀번호 암호화
        member.setPassword(password);
        member.setRole(Role.valueOf(memberFormDto.getRole()));
        return member;
    }
}
