package com.pbl6.services.impl;


import com.pbl6.dtos.request.checkout.CheckoutPickupRequest;
import com.pbl6.dtos.request.checkout.CheckoutShipRequest;
import com.pbl6.dtos.request.checkout.OrderRequest;
import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.OrderEntity;
import com.pbl6.entities.StoreEntity;
import com.pbl6.entities.UserAddressEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.enums.ReceiveMethod;
import com.pbl6.mapper.OrderRequestMapper;
import com.pbl6.mapper.UserAddressMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.*;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final UserRepository userRepo;
    private final VariantRepository variantRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentService paymentService;
    private final UserService userService;
    private final UserAdressRepository userAdressRepository;
    private final EntityUtil entityUtil;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductSerialRepository productSerialRepository;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final UserAddressMapper userAddressMapper;
    private final OrderRequestMapper orderRequestMapper;

    @Override
    @Transactional
    public PaymentInitResponse processCheckoutShipment(UserEntity user, CheckoutShipRequest req) {
        UserAddressEntity userAddress = userAddressMapper.toEntity(req);
        OrderRequest orderRequest = orderRequestMapper.fromShip(req);
        orderRequest.setUserId(user.getId());
        orderRequest.setEmail(user.getEmail());
        orderRequest.setFullName(user.getName());
        orderRequest.setPhone(user.getPhone());
        OrderEntity order = orderService.createOrder(orderRequest
        );

        //giữ hàng
        inventoryService.handleShip(order.getOrderItems());

        PaymentRequest payReq = new PaymentRequest(
                order.getId(),
                order.getTotalAmount(),
                req.getPaymentMethod(),
                ReceiveMethod.SHIPMENT
        );

        PaymentInitResponse payRes = paymentService.create(payReq);

        return payRes;
    }

    @Override
    @Transactional
    public PaymentInitResponse processCheckoutPickup(UserEntity user, CheckoutPickupRequest req) {

        StoreEntity store = entityUtil.ensureExists(storeRepository.findById(req.getStoreId()));

        OrderRequest orderRequest = orderRequestMapper.fromPickup(req);
        orderRequest.setUserId(user.getId());
        orderRequest.setEmail(user.getEmail());
        orderRequest.setFullName(user.getName());
        orderRequest.setPhone(user.getPhone());
        //Tạo đơn hàng
        OrderEntity order = orderService.createOrder(orderRequest);

        //Giữ hàng
        inventoryService.handlePickupAtStore(store, order.getOrderItems());

        //Tạo yêu cầu payment
        PaymentRequest payReq = new PaymentRequest(
                order.getId(),
                order.getTotalAmount(),
                req.getPaymentMethod(),
                ReceiveMethod.RECEIVE_AT_STORE
        );

        PaymentInitResponse payRes = paymentService.create(payReq);

        return payRes;
    }

}
