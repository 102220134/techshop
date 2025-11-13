package com.pbl6.services.impl;

import com.pbl6.dtos.response.StoreDto;
import com.pbl6.entities.StoreEntity;
import com.pbl6.mapper.StoreMapper;
import com.pbl6.repositories.StoreRepository;
import com.pbl6.services.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    @Override
    public List<StoreDto> getAllStore() {
        return storeRepository.findAll().stream()
                .map(storeMapper::toDto)
                .toList();
    }
}
