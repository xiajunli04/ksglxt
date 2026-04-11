package com.xjl.domain.dto.request;

import lombok.Data;

@Data
public class ProfileUpdateRequest {

    private String nickName;

    private String phone;

    private String email;

    private String sex;
}
