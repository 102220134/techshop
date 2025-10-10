package com.pbl6.services;

import com.pbl6.dtos.request.checkout.CheckoutPickupRequest;
import com.pbl6.dtos.request.checkout.CheckoutShipRequest;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.UserEntity;

public interface CheckoutService {
    PaymentInitResponse processCheckoutShipment(UserEntity user, CheckoutShipRequest req);
    PaymentInitResponse processCheckoutPickup(UserEntity user, CheckoutPickupRequest req);
}
