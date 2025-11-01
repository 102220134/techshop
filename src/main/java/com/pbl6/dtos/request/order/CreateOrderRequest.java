package com.pbl6.dtos.request.order;

import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.ReceiveMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrderRequest {

    // Người mua (user/customer)
    @NotBlank
    Long userId;

    // Cửa hàng xử lý đơn (bắt buộc nếu là nhân viên / admin)
    Long storeId;

    // Nhân viên phụ trách (optional, admin có thể chọn)
    Long saleId;

    ReceiveMethod receiveMethod;

    PaymentMethod paymentMethod;

    // Thông tin người nhận
    @NotBlank
    String fullName;
    @NotBlank
    String phone;

    String email;

    // Địa chỉ giao hàng
    String line;
    String ward;
    String district;
    String province;

    // Ghi chú đơn hàng
    String note;

    // Đơn hàng online/offline
    Boolean isOnline = false;

    // Danh sách sản phẩm
    @NotEmpty
    List<OrderItemRequest> items;
}
