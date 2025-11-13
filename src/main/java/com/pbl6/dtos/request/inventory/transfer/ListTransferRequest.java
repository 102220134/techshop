package com.pbl6.dtos.request.inventory.transfer;

import com.pbl6.enums.GRStatus;
import com.pbl6.enums.TransferStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListTransferRequest {
    private TransferStatus status;
    private String order = "createdAt";
    private String dir = "desc";
    private int page = 1;
    private int size = 20;
}
