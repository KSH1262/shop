package com.shop.shop.service;

import com.shop.shop.dto.*;
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

        // 1. 상품 등록
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        // 2. 이미지 등록
        for (int i = 0; i < itemImgFileList.size(); i++) {
            MultipartFile file = itemImgFileList.get(i);

            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);
            itemImg.setRepImgYn(i == 0 ? "Y" : "N");

            if (!file.isEmpty()) {
                String imgUrl = r2StorageService.uploadFile(file, "item_images");

                String oriImgName = file.getOriginalFilename();
                String imgName = UUID.randomUUID().toString() +
                        oriImgName.substring(oriImgName.lastIndexOf("."));

                itemImg.updateItemImg(oriImgName, imgName, imgUrl);
            } else {
                log.warn("[이미지 {}] 파일 비어있음 - 업로드 스킵", i);
            }

            itemImgService.saveItemImg(itemImg);
        }

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

        if (item.getIs_deleted()) {
            throw new EntityNotFoundException("요청하신 상품이 존재하지 않습니다.");
        }

        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }

    @Transactional(readOnly = true)
    public ItemFormDto getItemDtlByUuid(UUID uuid, String currentUserEmail) {
        Item item = itemRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다."));

        if (item.getIs_deleted()) {
            throw new EntityNotFoundException("요청하신 상품이 존재하지 않습니다.");
        }

        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(item.getId());
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();
        for (ItemImg itemImg : itemImgList){
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
        }

        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }

    @Transactional
    public Long updateItemByUuid(UUID uuid, ItemFormDto itemFormDto,
                                 List<MultipartFile> itemImgFileList, String currentUserEmail) throws Exception {

        Item item = itemRepository.findByUuid(uuid)
                .orElseThrow(EntityNotFoundException::new);

        if (!item.getCreatedBy().equals(currentUserEmail)) {
            throw new AccessDeniedException("작성자만 상품을 수정할 수 있습니다.");
        }

        // 상품 정보 업데이트
        item.updateItem(itemFormDto);

        List<Long> itemImgIds = itemFormDto.getItemImgIds();
        if (itemImgIds == null || itemImgIds.isEmpty()) {
            return item.getId();
        }

        for (int i = 0; i < itemImgIds.size(); i++) {
            MultipartFile file = (itemImgFileList != null && itemImgFileList.size() > i)
                    ? itemImgFileList.get(i) : null;

            // 파일이 없으면 스킵 (기존 이미지 유지)
            if (file == null || file.isEmpty()) continue;

            ItemImg savedItemImg = itemImgRepository.findById(itemImgIds.get(i))
                    .orElseThrow(EntityNotFoundException::new);

            // 새 파일 업로드
            String newImgUrl = r2StorageService.uploadFile(file, "item_images");
            String oriImgName = file.getOriginalFilename();
            String imgName = UUID.randomUUID().toString() +
                    oriImgName.substring(oriImgName.lastIndexOf("."));

            // 기존 URL은 나중에 삭제
            String oldImgUrl = savedItemImg.getImgUrl();

            savedItemImg.updateItemImg(oriImgName, imgName, newImgUrl);
            itemImgRepository.saveAndFlush(savedItemImg);

            // 기존 이미지 삭제 (DB 반영 후)
            if (oldImgUrl != null && !oldImgUrl.isEmpty()) {
                try {
                    r2StorageService.deleteFile(oldImgUrl);
                } catch (Exception e) {
                    log.warn("기존 이미지 삭제 실패: {}", e.getMessage());
                }
            }

            log.info("이미지 수정 완료: itemImgId={}, newUrl={}", savedItemImg.getId(), newImgUrl);
        }

        return item.getId();
    }


    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable, String currentUserEmail) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public Page<Item> getSellerItemPage(ItemSearchDto itemSearchDto, Pageable pageable, String currentUserEmail) {
        return itemRepository.getSellerItemPage(itemSearchDto, pageable, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public List<AdminItemDto> getAllAdminItems() {
        return itemRepository.findAllItemDtos();
    }

    public void deleteItemByUuid(UUID uuid, String currentUserEmail) {
        Item item = itemRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다."));

        if (!item.getCreatedBy().equals(currentUserEmail)) {
            throw new AccessDeniedException("작성자만 상품을 삭제할 수 있습니다.");
        }

        itemRepository.delete(item);
    }

    public void toggleItemStatus(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. id: " + itemId));

        if (item.getIs_deleted()) {
            item.activate();
        } else {
            item.deactivate();
        }
        // @Transactional 어노테이션 덕분에 save()를 명시적으로 호출하지 않아도 됨
    }

    @Transactional
    public void softDeleteItemByUuid(UUID uuid, String currentUserEmail) {
        Item item = itemRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다."));

        if (!item.getCreatedBy().equals(currentUserEmail)) {
            throw new AccessDeniedException("작성자만 상품을 삭제할 수 있습니다.");
        }

        item.setIs_deleted(true);
    }
}
