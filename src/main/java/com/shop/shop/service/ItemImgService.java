package com.shop.shop.service;

import com.shop.shop.entity.ItemImg;
import com.shop.shop.repository.ItemImgRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {

    private final ItemImgRepository itemImgRepository;


    // R2에서 업로드된 이미지 정보를 DB에 저장
    public void saveItemImg(ItemImg itemImg) {
        itemImgRepository.save(itemImg);
    }

    // 이미지를 DB에서 업데이트
    public void updateItemImg(ItemImg itemImg) {
        itemImgRepository.save(itemImg);
    }

//    @Value("${itemImgLocation}") // .yml 에 등록한 itemImgLocation 가져옴
//    private String itemImgLocation;
//
//    private final ItemImgRepository itemImgRepository;
//
//    private final FileService fileService;
//
//    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{
//        String oriImgName = itemImgFile.getOriginalFilename();
//        String imgName = "";
//        String imgUrl = "";
//
//        //파일 업로드
//        if (!StringUtils.isEmpty(oriImgName)){
//            // 사용자가 상품 이미지 등록 했다면 저장 경로, 파일 이름, 파일 바이트 배열을 파라미터로 uploadFile 호출
//            imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
//            imgUrl = "/images/item/" + imgName; // 저장한 상품 이미지 불러올 경로 설정
//        }
//
//        // 상품 이미지 정보 저장
//        // oriImgName : 실제 로컬에 저장된 상품 이미지 파일 이름
//        // imgName : 업로드했던 상품 이미지 파일의 원래 이름
//        // imgUrl : 업로드 결과 로컬에 저장된 상품 이미지 파일을 불러오는 경로
//        itemImg.updateItemImg(oriImgName, imgName, imgUrl);
//        itemImgRepository.save(itemImg);
//    }
//
//    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception{
//        if (!itemImgFile.isEmpty()){ // 상품 이미지 수정한 경우 상품 이미지 업데이트
//            // 상품 이미지 아이디 이용, 기존에 저장한 상품 이미지 엔티티 조회
//            ItemImg savedItemImg = itemImgRepository.findById(itemImgId)
//                    .orElseThrow(EntityExistsException::new);
//
//            // 기존 이미지 삭제
//            if (!StringUtils.isEmpty(savedItemImg.getImgName())) { // 기존 등록된 상품 이미지 파일 있을 경우 해당 파일 삭제
//                fileService.deleteFile(itemImgLocation+"/"+savedItemImg.getImgName());
//            }
//
//            String oriImgName = itemImgFile.getOriginalFilename();
//            // 업데이트한 상품 이미지 파일 업로드
//            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
//            String imgUrl = "/images/item/" + imgName;
//            // 상품 등록때 처림 itemImgRepository.save() 호출X, savedItemImg 엔티티는 현재 영속 상태
//            // 데이터 변경하는 것만으로 변경 감지 기능 동작, 트랜잭션이 끝날 때 update 쿼리 실행됨
//            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl); // 변경된 상품 이미지 정보 세팅
//        }
//    }

}
