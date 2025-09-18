package com.pbl6.controllers.privates;

import com.pbl6.dtos.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/cart")
public class CartController {
    @PostMapping(value = "add")
    @Operation(summary = "Add to cart", security = { @SecurityRequirement(name = "bearerAuth") })
    ApiResponseDto<String> addToCart(){
        return new ApiResponseDto<>();
    }
}
