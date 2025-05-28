package com.shop.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class OrderItem extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;

    private int count;

    public static OrderItem createOrderItem(Item item, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item); // 주문할 상품
        orderItem.setCount(count); // 주문 수량
        orderItem.setOrderPrice(item.getPrice()); // 현재 시간 기준 상품 가격을 주문 가격으로 세팅

        item.removeStock(count); // 주문 수량만큼 상품 재고 수량 감소
        return orderItem;
    }

    public int getTotalPrice() { // 주문 가격과 주문 수량을 곱해서 주문한 총 가격 계산
        return orderPrice*count;
    }

    public void cancel() { // 주문 취소 시 주문 수량만큼 상품의 재고 +
        this.getItem().addStock(count);
    }
}
