package com.pbl6.dtos.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class UpdateStaffRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String name;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    // Optional – chỉ update nếu có
    private String password;

    @NotNull(message = "Giới tính không được để trống")
    private String gender;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate birth;

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean isActive;

    @NotNull
    private Boolean isGlobalStaff;

    @NotNull(message = "Role không được để trống")
    private Set<String> roles;

    // inventory location ids
    private List<Long> scops;
}
