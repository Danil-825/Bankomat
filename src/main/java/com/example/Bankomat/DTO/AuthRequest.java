package com.example.Bankomat.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на аутентификацию")
public class AuthRequest {
    @Schema(description = "Логин (номер карты для клиента/email для персонала)", example = "1234567890123456")
    private String login;

    @Schema(description = "Пароль/PIN-код", example = "1234")
    private String password;
}
