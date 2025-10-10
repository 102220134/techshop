package com.pbl6.services;

import com.pbl6.dtos.request.checkout.PaymentRequest;
import com.pbl6.enums.DebtStatus;

public interface DebtService {
    void createOrUpdate(PaymentRequest req, DebtStatus debtStatus);
}
