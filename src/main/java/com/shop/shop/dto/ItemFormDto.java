package com.shop.shop.dto;

import com.shop.shop.constant.ItemSellStatus;
import com.shop.shop.entity.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemFormDto {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력 입니다.")
    private String itemNm;

    @NotNull(message = "가격은 필수 입력 입니다.")
    private Integer price;

    @NotBlank(message = "이름은 필수 입력 입니다.")
    private String itemDetail;

    @NotNull(message = "재고는 필수 입력 입니다.")
    private Integer stockNumber;

    private ItemSellStatus itemSellStatus;

    private List<ItemImgDto> itemImgDtoList = new ArrayList<>(); // 저장 후 수정 시 상품 이미지 정보 저장

    private List<Long> itemImgIds = new ArrayList<>(); // 상품 이미지 아이디 저장

    private static ModelMapper modelMapper = new ModelMapper();

    public Item createItem(){
        return modelMapper.map(this, Item.class);
    }

    public static ItemFormDto of(Item item){
        return modelMapper.map(item, ItemFormDto.class);
    }

}
