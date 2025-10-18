package com.pbl6.utils;

import com.pbl6.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderAutoCancelService {

    private final OrderService orderService;

    @Scheduled(fixedRate = 60000)
    public void cancelUnpaidOrders() {
        orderService.cancelOrderPaymentTimeout();
    }
}
