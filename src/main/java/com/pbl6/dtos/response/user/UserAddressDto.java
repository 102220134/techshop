package com.pbl6.dtos.response.user;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressDto {
    private Long id;
    private String phone;
    private String line;
    private String ward;
    private String district;
    private String province;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
