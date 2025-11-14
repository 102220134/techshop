package com.pbl6.dtos.request.inventory.GR;

import com.pbl6.enums.GRStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListGRRequest {
    private GRStatus status;
    private String order = "createdAt";
    private String dir = "desc";
    private int page = 1;
    private int size = 20;
}
