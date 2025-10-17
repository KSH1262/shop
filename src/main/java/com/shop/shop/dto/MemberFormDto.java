package com.shop.shop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class MemberFormDto {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식으로 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Length(min = 8, max = 16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요")
    private String password;

    // 주소 세부 입력용
    private String zipcode;
    private String roadAddress;
    private String detailAddress;

    // 실제 저장될 전체 주소
    private String address;

    @NotBlank(message = "회원 유형은 필수 선택입니다.")
    private String role;

    public void mergeAddress() {
        StringBuilder sb = new StringBuilder();
        if (zipcode != null && !zipcode.isBlank()) sb.append("(").append(zipcode).append(") ");
        if (roadAddress != null && !roadAddress.isBlank()) sb.append(roadAddress).append(" ");
        if (detailAddress != null && !detailAddress.isBlank()) sb.append(detailAddress);
        this.address = sb.toString().trim();
    }
}
