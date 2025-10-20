package com.pbl6.dtos.request.order;

import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.ReceiveMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {

    Long userId;
    Long storeId;
    ReceiveMethod receiveMethod;
    PaymentMethod paymentMethod;

    String fullName;
    String phone;
    String email;

    // địa chỉ (nếu là shipment)
    String line;
    String ward;
    String district;
    String province;
    String country;

    BigDecimal totalAmount;
    BigDecimal shippingFee;
    BigDecimal discountAmount;

    Boolean isOnline;

    List<OrderItemRequest> items;
}
