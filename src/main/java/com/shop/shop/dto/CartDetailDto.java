package com.shop.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDetailDto {

    private Long cartItemId;

    private String itemNm;

    private int price;

    private int count;

    private String imgUrl;

    // 장바구니 페이지에 전달할 데이터를 생성자의 파라미터로 만들어줌
    public CartDetailDto(Long cartItemId, String itemNm, int price, int count, String imgUrl){
        this.cartItemId = cartItemId;
        this.itemNm = itemNm;
        this.price = price;
        this.count = count;
        this.imgUrl = imgUrl;
    }
}
