package com.shop.shop.service;

import com.shop.shop.dto.ItemFormDto;
import com.shop.shop.dto.ItemImgDto;
import com.shop.shop.dto.ItemSearchDto;
import com.shop.shop.dto.MainItemDto;
import com.shop.shop.entity.Item;
import com.shop.shop.entity.ItemImg;
import com.shop.shop.repository.ItemImgRepository;
import com.shop.shop.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;
    private final ItemImgRepository itemImgRepository;
    private final R2StorageService r2StorageService;

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {

        log.info("=== [saveItem 시작] ===");
        log.info("상품명: {}", itemFormDto.getItemNm());
        log.info("이미지 파일 개수: {}", itemImgFileList != null ? itemImgFileList.size() : 0);

        // 1. 상품 등록
        Item item = itemFormDto.createItem();
        itemRepository.save(item);
        log.info("[상품 DB 저장 완료] 상품ID={}", item.getId());

        // 2. 이미지 등록
        for (int i = 0; i < itemImgFileList.size(); i++) {
            MultipartFile file = itemImgFileList.get(i);
            log.info("[이미지 {}] 원본 파일명: {}, 비어있는지 여부: {}", i, file.getOriginalFilename(), file.isEmpty());

            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);
            itemImg.setRepImgYn(i == 0 ? "Y" : "N");

            if (!file.isEmpty()) {
                log.info("[이미지 {}] R2 업로드 시도", i);
                String imgUrl = r2StorageService.uploadFile(file, "item_images");
                log.info("[이미지 {}] 업로드 완료 → URL: {}", i, imgUrl);

                String oriImgName = file.getOriginalFilename();
                String imgName = UUID.randomUUID().toString() +
                        oriImgName.substring(oriImgName.lastIndexOf("."));
                log.info("[이미지 {}] 저장용 파일명: {}", i, imgName);

                itemImg.updateItemImg(oriImgName, imgName, imgUrl);
            } else {
                log.warn("[이미지 {}] 파일 비어있음 - 업로드 스킵", i);
            }

            itemImgService.saveItemImg(itemImg);
            log.info("[이미지 {}] DB 저장 완료", i);
        }

        log.info("=== [saveItem 완료] 상품ID={} ===", item.getId());
        return item.getId();
    }

    @Transactional(readOnly = true) // 상품 데이터를 읽어오는 트랜잭션을 읽기 전용으로 설정, JPA가 더티체킹 수행X
    public ItemFormDto getItemDtl(Long itemId){

        //행당 상품의 이미지 조회, 등록순으로 가져오기 위해 오름차순
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();
        for (ItemImg itemImg : itemImgList){ // 조회한 ItemImg 엔티티를 ItemImgDto 객체로 만들어 리스트에 추가
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
        }

        // 상품 아이디를 통해 상품 엔티티 조회, 없으면 EntityNotFoundException 발생
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }

    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList, String currentUserEmail) throws Exception{

        // 상품 수정
        // 상품 등록 화면으로부터 전달 받은 상품 아이디 이용 상품 엔티티 조회
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);

        if (!item.getCreatedBy().equals(currentUserEmail)) { // 현재 로그인한 사용자와 작성자가 다르면
            throw new AccessDeniedException("작성자만 상품을 수정할 수 있습니다."); // 접근 거부 예외 발생
        }

        item.updateItem(itemFormDto); // 전달 받은 ItemFormDto 를 통해 상품 엔티티 업데이트

        List<Long> itemImgIds = itemFormDto.getItemImgIds(); // 상품 이미지 아이디 리스트 조회

        // 이미지 업데이트
        for (int i = 0; i<itemImgFileList.size(); i++){
            if(!itemImgFileList.get(i).isEmpty()){
                // 기존 이미지 파일이 있다면 R2에서 삭제
                ItemImg savedItemImg = itemImgRepository.findById(itemImgIds.get(i))
                        .orElseThrow(EntityNotFoundException::new);
                if (savedItemImg.getImgUrl() != null && !savedItemImg.getImgUrl().isEmpty()) {
                    r2StorageService.deleteFile(savedItemImg.getImgUrl());
                }

                // 새로운 이미지 R2에 업로드
                String imgUrl = r2StorageService.uploadFile(itemImgFileList.get(i), "item_images");
                String oriImgName = itemImgFileList.get(i).getOriginalFilename();
                String imgName = UUID.randomUUID().toString() + oriImgName.substring(oriImgName.lastIndexOf("."));
                savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);

                // 수정된 ItemImgService의 메서드 호출
                itemImgService.updateItemImg(savedItemImg);
            }
        }
        return item.getId();
    }

    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable, String currentUserEmail) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }

    public void deleteItem(Long itemId, String currentUserEmail) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다. id: " + itemId));

        if (!item.getCreatedBy().equals(currentUserEmail)) { // 현재 로그인한 사용자와 작성자가 다르면
            throw new AccessDeniedException("작성자만 상품을 삭제할 수 있습니다."); // 접근 거부 예외 발생
        }

        // is_deleted 값을 true로 변경하여 소프트 삭제를 수행합니다.
        item.setIs_deleted(true);
    }
}
