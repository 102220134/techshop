package com.pbl6.dtos.request.order;

import com.pbl6.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // Added @Setter for potential deserialization from request body
public class MyOrderRequest {
    Integer size = 5;
    Integer page = 1;
    @Schema(allowableValues = {"create_at", "total_amount"})
    String order = "create_at";
    @Schema(allowableValues = {"asc", "desc"})
    String dir ="desc";
    OrderStatus orderStatus;
}
