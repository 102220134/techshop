package com.pbl6.dtos.response.inventory;
import com.pbl6.dtos.response.product.VariantDto;
import lombok.Data;
import java.util.List;

@Data
public class InventoryDto {
    private Long productId;
    private String productName;
    private List<VariantDto> variants;
}