package com.example.Bankomat.controllers;

import com.example.Bankomat.DTO.AuthRequest;
import com.example.Bankomat.DTO.AuthResponse;
import com.example.Bankomat.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации пользователей")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Вход для клиентов",
            description = "Аутентификация по номеру карты и PIN-коду",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная аутентификация",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные учетные данные",
                            content = @Content(schema = @Schema())
                    )
            }
    )

    @PostMapping("/login/client")
    public ResponseEntity<?> loginClient(@RequestBody @Valid AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.authenticateClient(request));
        } catch (RuntimeException e) {
            int remainingAttempts = authService.getRemainingAttempts(request.getLogin());
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("remainingAttempts", remainingAttempts);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Operation(
            summary = "Вход для персонала",
            description = "Аутентификация по email и паролю",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная аутентификация",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверные учетные данные",
                            content = @Content(schema = @Schema())
                    )
            }
    )
    @PostMapping("/login/staff")
    public ResponseEntity<AuthResponse> loginStaff(@RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.authenticateStaff(request));
    }
    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getRemoteAddr();
        }
        return "unknown";
    }
}