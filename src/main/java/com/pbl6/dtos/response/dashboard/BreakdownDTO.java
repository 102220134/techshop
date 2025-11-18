package com.pbl6.dtos.response.dashboard;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakdownDTO {

    private String label;

    private Long count;
}
