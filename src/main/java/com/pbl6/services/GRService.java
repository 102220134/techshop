package com.pbl6.services;

import com.pbl6.dtos.request.inventory.GR.CreateGRRequest;
import com.pbl6.dtos.request.inventory.GR.GRDetailRequest;
import com.pbl6.dtos.request.inventory.GR.ListGRRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.GR.GRItemDto;
import com.pbl6.dtos.response.inventory.GR.GRDto;
import com.pbl6.dtos.response.inventory.SupplierDto;
import jakarta.validation.Valid;

import java.util.List;

public interface GRService {
    PageDto<GRDto> getGRs(ListGRRequest req);
    PageDto<GRItemDto> getGRItems(long id,GRDetailRequest req);

    List<SupplierDto> getSupplier();

    GRDto createGoodsReceipt(CreateGRRequest req);

    void deleteGoodsReceipt(Long id);
    void completeGoodsReceipt(Long id);

}
