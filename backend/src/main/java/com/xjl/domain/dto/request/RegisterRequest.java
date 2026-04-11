package com.xjl.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickName;

    private String phone;

    private String email;

    @NotBlank(message = "角色不能为空")
    private String roleCode;
}
