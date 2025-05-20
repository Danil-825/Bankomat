package com.example.Bankomat.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardOperationDTO {
    @Schema(description = "Номер карты", example = "1234567890123456", required = true)
    @NotBlank
    @Pattern(regexp = "^[0-9]{16}$", message = "Номер карты должен содержать 16 цифр")
    private String cardNumber;

    @Schema(description = "Сумма операции", example = "1000.00", required = true)
    @NotNull
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal summa;

    @Schema(description = "Числовой номер банкомата в городе", example = "1", required = true)
    @NotNull
    @Min(value = 1)
    private Long atmId;
}
