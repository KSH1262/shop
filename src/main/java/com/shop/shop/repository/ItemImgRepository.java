package com.shop.shop.repository;

import com.shop.shop.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {
    List<ItemImg> findByItemIdOrderByIdAsc (Long itemId);

    ItemImg findByItemIdAndRepImgYn(Long itemId, String repImgYn);

}
