package com.shop.shop.controller;

import com.shop.shop.dto.ItemFormDto;
import com.shop.shop.dto.ItemSearchDto;
import com.shop.shop.entity.Item;
import com.shop.shop.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ConcurrentHashMap<String, Boolean> idempotencyTokenCache;

    @GetMapping("/seller/item/new")
    public String itemForm(Model model, HttpServletResponse response){
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxy 캐시 방지

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");

        model.addAttribute("itemFormDto", new ItemFormDto());
        model.addAttribute("idempotencyToken", UUID.randomUUID().toString());
        return "item/itemForm";
    }

    @PostMapping("/seller/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                          @RequestParam("idempotencyToken") String idempotencyToken) {

        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }

        if (idempotencyTokenCache.putIfAbsent(idempotencyToken, Boolean.TRUE) != null) {
            log.warn("중복 요청 감지 (상품 등록): {}", idempotencyToken);
            model.addAttribute("errorMessage", "이미 처리된 요청입니다. 다시 시도하려면 새로고침 해주세요.");
            return "item/itemForm";
        }

        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            idempotencyTokenCache.remove(idempotencyToken);
            log.error("[상품 저장 중 예외 발생]", e);
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/";
    }

    @GetMapping("/seller/item/{uuid}")
    public String itemDtlByUuid(@PathVariable("uuid") UUID uuid, Model model, Authentication authentication) {
        try {
            String currentUserEmail = authentication.getName();
            ItemFormDto itemFormDto = itemService.getItemDtlByUuid(uuid, currentUserEmail);
            model.addAttribute("itemFormDto", itemFormDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            return "item/itemForm";
        } catch (AccessDeniedException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("itemFormDto", new ItemFormDto());
            return "item/itemForm";
        }
        return "item/itemForm";
    }

    @PostMapping("/seller/item/{uuid}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto,
                             BindingResult bindingResult,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                             Model model,
                             Authentication authentication,
                             @PathVariable("uuid") UUID uuid) {

        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }

        boolean isCreate = (itemFormDto.getId() == null);

        if (isCreate && (itemImgFileList == null || itemImgFileList.isEmpty() || itemImgFileList.get(0).isEmpty())) {
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }

        try {
            String currentUserEmail = authentication.getName();
            itemService.updateItemByUuid(uuid, itemFormDto, itemImgFileList, currentUserEmail);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "상품 수정 중 오류가 발생했습니다.");
            return "item/itemForm";
        }

        return "redirect:/seller/items"; // 수정 완료 후 판매자 상품 목록으로 이동
    }

    @GetMapping({"/seller/items","/seller/items/{page}"})
    public String sellerItemManage(ItemSearchDto itemSearchDto,
                                   @PathVariable("page") Optional<Integer> page,
                                   Model model,
                                   Authentication authentication) {
        String currentUserEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page.orElse(0), 3);

        Page<Item> items = itemService.getSellerItemPage(itemSearchDto, pageable, currentUserEmail);
        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);
        model.addAttribute("currentLoggedInUserEmail", currentUserEmail);

        return "item/itemMng";
    }

//    @GetMapping({"/admin/items","/admin/items/{page}"})
//    public String adminItemManage(ItemSearchDto itemSearchDto,
//                                  @PathVariable("page") Optional<Integer> page,
//                                  Model model,
//                                  Authentication authentication) {
//        String currentUserEmail = authentication.getName();
//        Pageable pageable = PageRequest.of(page.orElse(0), 3);
//
//        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable, currentUserEmail);
//        model.addAttribute("items", items);
//        model.addAttribute("itemSearchDto", itemSearchDto);
//        model.addAttribute("maxPage", 5);
//        model.addAttribute("currentLoggedInUserEmail", currentUserEmail);
//
//        return "item/itemMng";
//    }

    @GetMapping("/item/{uuid}")
    public String itemDtl(Model model, @PathVariable("uuid") UUID uuid){
        try {
            ItemFormDto itemFormDto = itemService.getItemDtlByUuid(uuid, null);
            model.addAttribute("itemFormDto", itemFormDto);
            model.addAttribute("item", itemFormDto);

        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            model.addAttribute("idempotencyToken", UUID.randomUUID().toString());
            return "item/itemNotFound";
        }
        return "item/itemDtl";
    }

    @DeleteMapping("/seller/item/{uuid}")
    @ResponseBody
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<String> deleteItem(@PathVariable("uuid") UUID uuid,
                                             Authentication authentication) {
        try {
            String currentUserEmail = authentication.getName();
            itemService.softDeleteItemByUuid(uuid, currentUserEmail);
            return new ResponseEntity<>("상품이 성공적으로 삭제되었습니다.", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>("상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("상품 삭제에 실패했습니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
