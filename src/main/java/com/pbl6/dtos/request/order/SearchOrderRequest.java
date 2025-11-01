package com.pbl6.dtos.request.order;

import com.pbl6.enums.OrderStatus;
import com.pbl6.enums.PaymentMethod;
import com.pbl6.enums.ReceiveMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchOrderRequest {
    @Schema(description = "Mã đơn hàng", example = "123", nullable = true)
    private Long id;

    @Schema(description = "Trạng thái đơn hàng", nullable = true, allowableValues = {"PENDING", "CONFIRMED", "DELIVERING", "COMPLETED", "CANCELLED", "REFUSED", "RETURNED"})
    private OrderStatus status;

    @Schema(description = "Loại đơn hàng: ONLINE / OFFLINE (hoặc tại cửa hàng)", nullable = true)
    private Boolean isOnline;

    @Schema(description = "ID cửa hàng (nếu admin chọn lọc theo store)", example = "1", nullable = true)
    private Long storeId;

    @Schema(description = "Phương thức nhận hàng", nullable = true, allowableValues = {"PICKUP", "DELIVERY"})
    private ReceiveMethod receiveMethod;

    @Schema(description = "Phương thức thanh toán", nullable = true, allowableValues = {"COD", "BANK", "VNPAY"})
    private PaymentMethod paymentMethod;

    @Schema(description = "Tìm theo tên hoặc số điện thoại khách hàng", nullable = true)
    private String customerKeyword;

    @Schema(description = "Thời gian tạo đơn từ ngày", nullable = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @Schema(description = "Thời gian tạo đơn đến ngày", nullable = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    @Schema(description = "Số trang, mặc định là 0", example = "0", nullable = true)
    private Integer page = 0;

    @Schema(description = "Kích thước trang, mặc định là 20", example = "20", nullable = true)
    private Integer size = 20;

    @Schema(description = "Sắp xếp theo trường, mặc định là createdAt", example = "createdAt", nullable = true)
    private String sort = "createdAt";

    @Schema(description = "Thứ tự sắp xếp, mặc định là DESC", example = "DESC", nullable = true)
    private String dir = "desc";
}