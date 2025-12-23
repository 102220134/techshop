package com.pbl6.services.impl;

import com.pbl6.dtos.request.inventory.reservation.ListReservationRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.StoreDto;
import com.pbl6.dtos.response.inventory.InventoryLocationDto;
import com.pbl6.dtos.response.inventory.reservation.ReservationDto;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.entities.*;
import com.pbl6.mapper.StoreMapper;
import com.pbl6.repositories.InventoryLocationRepository;
import com.pbl6.repositories.ReservationRepository;
import com.pbl6.services.InventoryLocationService;
import com.pbl6.services.ReservationService;
import com.pbl6.utils.AuthenticationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final StoreMapper storeMapper;
    private final InventoryLocationService inventoryLocationService;
    private final AuthenticationUtil authenticationUtil;

    public ReservationServiceImpl(ReservationRepository reservationRepository, StoreMapper storeMapper, InventoryLocationRepository inventoryLocationRepository, InventoryLocationService inventoryLocationService, AuthenticationUtil authenticationUtil) {
        this.reservationRepository = reservationRepository;
        this.storeMapper = storeMapper;
        this.inventoryLocationService = inventoryLocationService;
        this.authenticationUtil = authenticationUtil;
    }

    @Override
    public PageDto<ReservationDto> listReservations(ListReservationRequest req) {
        UserEntity user = authenticationUtil
                .getCurrentUser();

        Sort sort = Sort.by(
                req.getDir().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                req.getOrder()
        );
        PageRequest pageable = PageRequest.of(req.getPage() - 1, req.getSize(), sort);

        Set<Long> scopsLocationIds = null;
        if (!user.isAdmin() && !user.getIsGlobalStaff()) {
            scopsLocationIds = user.getScops()
                    .stream()
                    .map(InventoryLocationEntity::getId)
                    .collect(Collectors.toSet());
            if (scopsLocationIds.isEmpty()) {
                return PageDto.empty(pageable);
            }
        }

        Page<ReservationEntity> pageResult = reservationRepository.searchReservations(
                req.getStatus(),
                req.getReceiveMethod(),
                scopsLocationIds,
                pageable
        );

        return new PageDto<>(pageResult.map(this::toDto));
    }

    private ReservationDto toDto(ReservationEntity e){
        OrderEntity order = e.getOrder();
        OrderItemEntity orderItem = e.getOrderItem();
        StoreDto storeDto = null;
        if(order.getStore()!=null){
            storeDto = storeMapper.toDto(order.getStore());
        }


        return ReservationDto.builder()
                .id(e.getId())
                .source(inventoryLocationService.toDto(e.getLocation()))
                .orderId(order.getId())
                .destination(storeDto)
                .status(e.getStatus())
                .receiveMethod(order.getReceiveMethod())
                .deliveryAddress(order.getSnapshot().getDeliveryAddress())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .sku(orderItem.getSku())
                .thumbnail(orderItem.getThumbnail())
                .attributes(
                        orderItem.getVariant().getVariantAttributeValues().stream()
                                .map(vav -> VariantDto.AttributeDto.builder()
                                        .code(vav.getAttribute().getCode())
                                        .label(vav.getAttribute().getLabel())
                                        .value(vav.getAttributeValue().getLabel())
                                        .build())
                                .toList()
                )
                .serials(e.getProductSerials().stream().map(ps -> ps.getSerialNo()).toList())
                .quantity(e.getQuantity())
                .build();
    }
}
