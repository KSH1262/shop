package com.shop.shop.controller;

import com.shop.shop.dto.ItemFormDto;
import com.shop.shop.dto.ItemSearchDto;
import com.shop.shop.entity.Item;
import com.shop.shop.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/admin/item/new")
    public String itemForm(Model model){
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "item/itemForm";
    }

    @PostMapping("/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList){

        if (bindingResult.hasErrors()){ // 상품 등록 시 필수 값이 없다면 상품 등록 페이지로 전환
            return "item/itemForm";
        }

        // 상품 등록 시 첫 번째 이미지가 없다면 에러 메시지와 함께 상품 등록 페이지로 전환, 필수 값임
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입나다.");
            return "item/itemForm";
        }

        try {
            itemService.saveItem(itemFormDto, itemImgFileList); // 상품 저장 로직 호출
        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/"; // 상품 정상 등록되면 메인 페이지로
    }

    @GetMapping("/admin/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model){

        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId); // 조회한 상품 데이터 모델에 담아 뷰로 전달
            model.addAttribute("itemFormDto", itemFormDto);
        } catch (EntityNotFoundException e){ // 상품 엔티티가 존재하지 않으면 에러메시지 담아 상품 등록 페이지로 이동
            model.addAttribute("errorMessage", "존재하지 않는 상품 입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            return "item/itemForm";
        }
        return "item/itemForm";
    }

    @PostMapping("/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult
            ,@RequestParam("itemImgFile") List<MultipartFile> itemImgFileList, Model model){

        if (bindingResult.hasErrors()){
            return "item/itemForm";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번재 상품 이미지는 필수 입력 값 입니다.");
            return "item/itemForm";
        }

        try {
            itemService.updateItem(itemFormDto, itemImgFileList); // 상품 수정 로직 호출
        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return  "item/itemForm";
        }

        return "redirect:/";
    }

    @GetMapping({"/admin/items","/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page")Optional<Integer> page, Model model){
        // 페이징을 위해 PageRequest.of 메소드를 통새 Pageable 객체 생성
        // 첫 번째 파라미터 : 조회할 페이지 번호, 두 번째 : 한 번에 가지고 올 데이터 수
        // URL 경로에 페이지 번호가 있으면 해당 페이지를 조회, 없으면 0페이지 조회
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 3);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);
        model.addAttribute("items", items); // 조회한 상품 데이터, 페이징 정보 뷰에 전달
        // 페이지 전환 시 기존 검색 조건을 유지한 재 이동할 수 있도록 뷰에 다시 전달
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5 ); // 상품 관리 메뉴 하단에 보여줄 페이지 최대 개수
        return "item/itemMng";
    }

    @GetMapping("/item/{itemId}")
    public String itemDtl(Model model, @PathVariable("itemId") Long itemId){
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId); // itemId로 상품 정보 로드
            model.addAttribute("itemFormDto", itemFormDto);
            model.addAttribute("item", itemFormDto);

        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto()); // 오류 발생 시 빈 객체
            return "item/itemNotFound"; // 에러 페이지로 리다이렉트하거나 다른 처리
        }
        return "item/itemDtl"; // 상품 상세 페이지 템플릿 이름
    }
    @DeleteMapping("/admin/item/{itemId}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteItem(@PathVariable("itemId") Long itemId) {
        try {
            itemService.deleteItem(itemId);
            // 삭제 성공 시 200 OK 상태 코드와 메시지 반환
            return new ResponseEntity<>("상품이 성공적으로 삭제되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            // 삭제 실패 시 400 Bad Request 상태 코드와 에러 메시지 반환
            return new ResponseEntity<>("상품 삭제에 실패했습니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
