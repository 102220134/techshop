package com.pbl6.dtos.response.cart;

import com.pbl6.dtos.response.product.VariantDto;
import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    long id;
    VariantDto item;
    int quantity;
}
