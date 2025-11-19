package com.pbl6.services;

import com.pbl6.dtos.request.inventory.transfer.CreateTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.ListTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.TransferDetailRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.GR.GRDto;
import com.pbl6.dtos.response.inventory.GR.GRItemDto;
import com.pbl6.dtos.response.inventory.transfer.TransferDto;
import com.pbl6.dtos.response.inventory.transfer.TransferItemDto;
import com.pbl6.entities.InventoryEntity;
import com.pbl6.entities.InventoryLocationEntity;
import com.pbl6.entities.OrderItemEntity;
import com.pbl6.enums.TransferStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface TransferService {
    TransferDto createTransfer(CreateTransferRequest req);
    TransferDto createTransfer(List<Long> reservationIds);
    PageDto<TransferDto> getTransfers(ListTransferRequest req);

    PageDto<TransferItemDto> getTransferItems(long id, TransferDetailRequest request);

    void confirmTransfer(long id);

    void startTransfer(Long transferId);
    void completeTransfer(Long transferId);
    public void deleteTransfer(Long transferId);
    void updateTransferStatus(Long transferId, TransferStatus newStatus);

//    TransferDto createDelivery(long id);
}
