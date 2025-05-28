package com.shop.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartOrderDto {

    private Long cartItemId;

    // 장바구니에서 여러 개의 상품을 주문함, 그래서 자기 자신을 가지고 있도록함
    private List<CartOrderDto> cartOrderDtoList;
}
