package com.pbl6.services;

import com.pbl6.dtos.request.inventory.delivery.UpdateTrackingRequest;
import com.pbl6.dtos.response.inventory.delivery.DeliveryDto;
import com.pbl6.enums.DeliveryStatus;

import java.util.List;

public interface DeliveryService {

    /**
     * Tạo vận đơn (và giữ chỗ kho) cho một yêu cầu Reservation.
     * Được gọi bởi NV Kho khi xử lý đơn "Giao hàng tận nơi".
     */
    DeliveryDto createDelivery(List<Long> reservationId);

    /**
     * Cập nhật thông tin vận đơn thủ công (cho bên thứ 3).
     * Được gọi bởi NV Kho sau khi tạo đơn trên App GHTK/ViettelPost.
     */
    void updateTrackingInfo(Long deliveryId, UpdateTrackingRequest req);

    /**
     * Cập nhật trạng thái giao hàng VÀ xử lý toàn bộ logic kho bãi.
     * Đây là hàm "động cơ" chính, được gọi bởi OrderService.
     */
    void updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus);
}
