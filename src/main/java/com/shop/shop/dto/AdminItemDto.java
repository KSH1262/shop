package com.shop.shop.dto;

import com.shop.shop.constant.ItemSellStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminItemDto {
    private Long id;
    private String itemNm;
    private int price;
    private int stockNumber;
    private ItemSellStatus itemSellStatus;
    private String repImgUrl;
    private String createdBy;
    private boolean is_deleted;
}
