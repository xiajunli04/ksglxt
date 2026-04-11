package com.xjl.domain.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserInfoVO {

    private Long id;

    private String userName;

    private String nickName;

    private String email;

    private String phone;

    private String sex;

    private String avatar;

    private String status;

    private List<String> roles;

    private LocalDateTime createTime;
}
