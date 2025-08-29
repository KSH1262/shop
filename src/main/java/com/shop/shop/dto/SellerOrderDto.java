package com.shop.shop.dto;

import com.shop.shop.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SellerOrderDto {
    private Long orderId;
    private String buyerEmail;
    private String buyerAddress;
    private String itemNm;
    private int count;
    private LocalDateTime orderDate;
    private String orderStatus;
    private String imgUrl;

    public SellerOrderDto(Long orderId,
                          String buyerEmail,
                          String buyerAddress,
                          String itemNm,
                          int count,
                          LocalDateTime orderDate,
                          com.shop.shop.constant.OrderStatus orderStatus,
                          String imgUrl) {
        this.orderId = orderId;
        this.buyerEmail = buyerEmail;
        this.buyerAddress = buyerAddress;
        this.itemNm = itemNm;
        this.count = count;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus.name();
        this.imgUrl = imgUrl;
    }
}