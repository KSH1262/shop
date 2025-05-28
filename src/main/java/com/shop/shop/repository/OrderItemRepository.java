package com.shop.shop.repository;

import com.shop.shop.entity.ItemImg;
import com.shop.shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
