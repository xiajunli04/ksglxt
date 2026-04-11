package com.xjl.service;

import com.xjl.domain.R;
import com.xjl.domain.dto.request.LoginRequest;
import com.xjl.domain.dto.request.RegisterRequest;

import java.util.Map;

public interface IAuthService {

    R<Void> register(RegisterRequest request);

    R<Map<String, Object>> login(LoginRequest request);

    R<Void> logout();
}
