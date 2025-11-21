package com.pbl6.controllers.chat;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.chat.RoomDto;
import com.pbl6.services.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/private/chat/rooms")
public class RoomChatController {
    private final RoomService roomService;

    @PreAuthorize("hasAuthority('CHAT')")
    @Operation(
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping
    public ApiResponseDto<?> getAllRooms(
            @RequestParam(required = false,defaultValue = "1") int page,
            @RequestParam(required = false,defaultValue = "10")  int size
    ) {
        return new ApiResponseDto<>(roomService.getAllRooms(page, size));
    }
}
