package com.forensicdept.user.service;

import com.forensicdept.config.AppProperties;
import com.forensicdept.exception.ResourceNotFoundException;
import com.forensicdept.security.JwtTokenProvider;
import com.forensicdept.user.dto.LoginRequest;
import com.forensicdept.user.dto.LoginResponse;
import com.forensicdept.user.entity.UserEntity;
import com.forensicdept.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserEntity user = userRepository.findByUsernameAndIsActiveTrue(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", null));

        String token = tokenProvider.generateToken(user.getUsername(), user.getId(), user.getUserRole());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(appProperties.getJwt().getExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getUserRole())
                .build();
    }
}
