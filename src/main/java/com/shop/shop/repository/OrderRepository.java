package com.shop.shop.repository;

import com.shop.shop.dto.SellerOrderDto;
import com.shop.shop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o " +
            "where o.member.email = :email " +
            "order by o.orderDate desc"
    )
    List<Order> findOrders(@Param("email") String email, Pageable pageable); // 주문 데이터를 페이징 조건에 맟춰서 조회

    @Query("select count(o) from Order o " +
            "where o.member.email = :email"
    )
    Long countOrder(@Param("email") String email); // 주문 개수가 몇 개인지 조회

    @Query("select distinct o from Order o " +
            "join o.orderItems oi " +
            "join oi.item i " +
            "where i.createdBy = :sellerEmail " +
            "order by o.orderDate desc")
    List<Order> findOrdersBySellerEmail(@Param("sellerEmail") String sellerEmail);

    @Query("select distinct o from Order o join fetch o.orderItems oi join fetch oi.item i")
    List<Order> findAllWithOrderItemsAndItems();

    @Query("select new com.shop.shop.dto.SellerOrderDto(" +
            "o.id, m.email, m.address, i.itemNm, oi.count, " + // ✅ m.address 추가
            "o.orderDate, o.orderStatus, im.imgUrl) " +
            "from Order o " +
            "join o.member m " +
            "join o.orderItems oi " +
            "join oi.item i " +
            "left join ItemImg im on im.item = i and im.repImgYn = 'Y' " +
            "where i.createdBy = :sellerEmail " +
            "order by o.orderDate desc")
    List<SellerOrderDto> findSellerOrders(@Param("sellerEmail") String sellerEmail);
}
