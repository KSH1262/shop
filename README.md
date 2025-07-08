# 🛒 Spring Boot 쇼핑몰 프로젝트

 Spring Boot 기반의 쇼핑몰 웹 애플리케이션입니다. 상품 등록, 장바구니, 주문, 회원가입/로그인 등의 기능을 구현하였으며, Thymeleaf를 활용한 화면 렌더링과 Spring Security를 이용한 인증·인가 기능을 포함합니다. Docker와 AWS 배포도 연동 가능합니다.

---

## 🔧 주요 기술 스택

| 영역       | 기술                                                |
|------------|---------------------------------------------------|
| Backend    | Spring Boot 3.x, Spring Security, Spring Data JPA |
| Template   | Thymeleaf, Thymeleaf Layout Dialect               |
| Database   | MySQL                                             |
| ORM        | JPA, Hibernate, QueryDSL                          |
| Validation | Hibernate Validator (JSR-380)                     |
| Build Tool | Gradle                                            |
| Deploy     | Docker, Docker Compose                            |
| 기타       | ModelMapper, Lombok                               |

---

## ✨ 주요 기능

### 🔐 회원
- 회원가입 및 로그인 (Spring Security)
- 로그인 실패 시 사용자에게 에러 메시지 출력
- 비밀번호 암호화 저장 (BCrypt)

### 🛍️ 상품
- 관리자 상품 등록 및 수정
- 상품 상세 조회
- 대표 이미지 등록 기능

### 🛒 장바구니
- 상품을 장바구니에 담기
- 수량 변경 및 삭제
- 로그인한 사용자만 이용 가능

### 🧾 주문
- 장바구니 상품을 주문 처리
- 주문 내역 조회

### 🔎 검색 & 필터
- 상품명, 상품상세, 가격 등으로 검색
- QueryDSL을 이용한 동적 검색

---
