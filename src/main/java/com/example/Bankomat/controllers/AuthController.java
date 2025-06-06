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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<AuthResponse> loginClient(@RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.authenticateClient(request));
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
}