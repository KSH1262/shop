package com.shop.shop.repository;

import com.shop.shop.dto.CartDetailDto;
import com.shop.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartIdAndItemId(Long cartId, Long itemId); // 카트, 상품 아니디를 이용, 장바구니에 상품 있는지 조회

    @Query("select new com.shop.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl) " +
            "from CartItem ci, ItemImg im " +
            "join ci.item i " +
            "where ci.cart.id = :cartId " +
            "and im.item.id = ci.item.id " + // 장바구니에 있는 상품의 대표 이미지만 가지고 오도록 조건문
            "and im.repImgYn = 'Y' " + // 장바구니에 있는 상품의 대표 이미지만 가지고 오도록 조건문
            "order by ci.regTime desc"
    )
    List<CartDetailDto> findCartDetailDtoList(Long cartId);
}
