package com.shop.shop.service;

import com.shop.shop.dto.CartDetailDto;
import com.shop.shop.dto.CartItemDto;
import com.shop.shop.dto.CartOrderDto;
import com.shop.shop.dto.OrderDto;
import com.shop.shop.entity.Cart;
import com.shop.shop.entity.CartItem;
import com.shop.shop.entity.Item;
import com.shop.shop.entity.Member;
import com.shop.shop.repository.CartItemRepository;
import com.shop.shop.repository.CartRepository;
import com.shop.shop.repository.ItemRepository;
import com.shop.shop.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public Long addCart(CartItemDto cartItemDto, String email){

        // 장바구니에 담을 상품 엔티티 조회
        Item item = itemRepository.findByUuid(cartItemDto.getItemUuid()).orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 이메일을 가진 회원을 찾을 수 없습니다.")); // 현재 로그인한 회원 엔티티 조회

        if (item.getIs_deleted()) {
            throw new IllegalStateException("삭제된 상품은 장바구니에 담을 수 없습니다.");
        }

        Cart cart = cartRepository.findByMemberId(member.getId()); // 현재 로그인한 회원의 장바구니 엔티티 조회
        if (cart == null){ // 상품을 처음 장바구니에 담을 경우 해당 회원의 장바구니 엔티티 생성
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        // 현재 상품이장바구니에 이미 들어가 있는지 조회
        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if (savedCartItem != null){
            savedCartItem.addCount(cartItemDto.getCount()); // 장바구니에 있던 상품이면 기존 수량에 담을 수량 만큼 더함
            return savedCartItem.getId();
        } else {
            // 장바구니, 상품 엔티티, 장바구니에 담을 수량을 이용 CartItem 엔티티 생성
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem); // 장바구니에 들어갈 상품 저장
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email){

        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 이메일을 가진 회원을 찾을 수 없습니다."));
        Cart cart = cartRepository.findByMemberId(member.getId()); // 현재 로그인한 회원의 장바구니 엔티티 조회
        if (cart == null){ // 장바구니에 상품을 한 번도 안 담았을 경우 장바구니 엔티티가 없으므로 빈 리스트 반환
            return cartDetailDtoList;
        }

        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId()); // 장바구니에 담겨있는 상품 정보를 조회

        return cartDetailDtoList;
    }

    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email){
        Member curMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("현재 로그인한 회원을 찾을 수 없습니다.")); // 로그인한 회원 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        Member savedMember = cartItem.getCart().getMember(); //장바구니 상푸 저장한 회원 조회

        // 현재 로그인한 회원과 장바구니 상품을 저장한 회원이 다르면 false, 같으면 true
        if (!StringUtils.equals(curMember.getEmail(),savedMember.getEmail())){
            return false;
        }

        return true;
    }

    public void updateCartItemCount(Long cartItemId, int count){ // 장바구니 상품 수량 업데이트
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);

        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId){
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email){
        List<OrderDto> orderDtoList = new ArrayList<>();

        // 장바구니 페이지에서 전달받은 주문 상품 번호 이요, 주문 로직으로 전달할 orderDto 객체 생성
        for (CartOrderDto cartOrderDto : cartOrderDtoList){
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderDto orderDto = new OrderDto();
            orderDto.setItemUuid(cartItem.getItem().getUuid());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add((orderDto));
        }

        Long orderId = orderService.orders(orderDtoList, email); // 장바구니에 담은 상품을 주무하도록 주문 로직 호출

        for (CartOrderDto cartOrderDto : cartOrderDtoList){ // 주문한 상품 장바구니에서 제거
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);
            cartItemRepository.delete(cartItem);
        }

        return orderId;
    }

}
