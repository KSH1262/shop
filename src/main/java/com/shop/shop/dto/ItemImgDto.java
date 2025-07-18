package com.shop.shop.dto;

import com.shop.shop.entity.ItemImg;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class ItemImgDto {

    private Long id;

    private String imgName;

    private String oriImgName;

    private String imgUrl;

    private String  repImgYn;

    private static ModelMapper modelMapper = new ModelMapper();

    // ItemImgDto 객체를 생성하지 않아도 호출
    public static ItemImgDto of(ItemImg itemImg){
        return modelMapper.map(itemImg,ItemImgDto.class);
    }
}
