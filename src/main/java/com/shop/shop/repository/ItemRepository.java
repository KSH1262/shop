package com.shop.shop.repository;

import com.shop.shop.constant.ItemSellStatus;
import com.shop.shop.dto.AdminItemDto;
import com.shop.shop.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item>, ItemRepositoryCustom {
    List<Item> findByItemNm(String itemNm);
    List<Item> findByItemNmOrItemDetail(String itemNm, String itemDetail); //
    List<Item> findByPriceLessThan(Integer price);

    @Query("select i from Item i where i.itemNm like %:searchQuery% and i.is_deleted = false")
    Page<Item> findByItemNmAndIsDeletedFalse(@Param("searchQuery") String searchQuery, Pageable pageable);

    @Query("select i from Item i where i.itemSellStatus = :itemSellStatus " +
            "and i.itemNm like %:searchQuery% and i.is_deleted = false")
    Page<Item> findByItemSellStatusAndItemNmAndIsDeletedFalse
            (@Param("itemSellStatus") ItemSellStatus itemSellStatus,
             @Param("searchQuery") String searchQuery, Pageable pageable);

    @Query("select new com.shop.shop.dto.AdminItemDto(" +
            "i.id, i.itemNm, i.price, i.stockNumber, i.itemSellStatus, " +
            "coalesce(im.imgUrl, ''), i.createdBy, i.is_deleted) " +
            "from Item i " +
            "left join i.itemImgList im with im.repImgYn = 'Y'")
    List<AdminItemDto> findAllItemDtos();

}
