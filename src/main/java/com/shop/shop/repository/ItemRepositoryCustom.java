package com.shop.shop.repository;

import com.shop.shop.dto.ItemSearchDto;
import com.shop.shop.dto.MainItemDto;
import com.shop.shop.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {

    // 상품 조회 조건 : ItemSearchDto, 객체와 페이징 정보 : Pageable
    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable, String currentUserEmail);
    Page<Item> getSellerItemPage(ItemSearchDto itemSearchDto, Pageable pageable, String currentUserEmail);
    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
}
