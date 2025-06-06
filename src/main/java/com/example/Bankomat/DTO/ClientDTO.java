package com.example.Bankomat.DTO;

import com.fasterxml.jackson.databind.deser.impl.CreatorCandidate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные клиента")
public class ClientDTO {
    @Schema(description = "Полное имя", example = "Иванов Иван")
    private String fullName;

    @Schema(description = "Текущий баланс", example = "15000.50")
    private BigDecimal balance;

    @Schema(description = "Замаскированный номер карты", example = "•••• 3456")
    private String maskedCardNumber;
}
