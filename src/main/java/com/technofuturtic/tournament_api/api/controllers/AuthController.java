package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.api.models.user.requests.LoginRequest;
import com.technofuturtic.tournament_api.api.models.user.requests.RefreshTokenRequest;
import com.technofuturtic.tournament_api.api.models.user.requests.RegisterRequest;
import com.technofuturtic.tournament_api.api.models.user.responses.UserResponse;
import com.technofuturtic.tournament_api.api.models.user.responses.UserTokenResponse;
import com.technofuturtic.tournament_api.api.utils.JwtUtils;
import com.technofuturtic.tournament_api.api.utils.RateLimit;
import com.technofuturtic.tournament_api.bll.services.AuthService;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @RateLimit(maxRequests = 3, windowSeconds = 60)
    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    public ResponseEntity<UserTokenResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        UserEntity user = authService.register(request.toUser());

        UserTokenResponse response = mapUser(user);

        return ResponseEntity.ok(response);
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    public ResponseEntity<UserTokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        UserEntity user = authService.login(request.username(), request.password());

        UserTokenResponse response = mapUser(user);

        log.info("User {} logged in successfully", user.getUsername());

        return ResponseEntity.ok(response);
    }

    @RateLimit(maxRequests = 10, windowSeconds = 60)
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        if (!jwtUtils.validateRefreshToken(request.refreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        UserContext userContext = jwtUtils.getUser(request.refreshToken());

        UserEntity user = authService.findById(userContext.id());

        String newAccessToken = jwtUtils.generateToken(user);

        return ResponseEntity.ok(new UserTokenResponse(
                UserResponse.fromUser(user),
                newAccessToken,
                request.refreshToken()
        ));
    }

    private UserTokenResponse mapUser(UserEntity user) {

        UserResponse userResponse = UserResponse.fromUser(user);

        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        return new UserTokenResponse(userResponse, accessToken, refreshToken);
    }
}