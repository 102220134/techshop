package com.pbl6.dtos.request.inventory.GR;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GRDetailRequest {
    @Schema(allowableValues = {"quantity","unitCost"})
    private String order = "quantity";
    private String dir = "desc";
    private int page = 1;
    private int size = 20;
}
