package com.example.Bankomat.DTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDTO {
    private String transactionType;
    private BigDecimal summa;
    private LocalDateTime timestamp;
    private String description;
    private BigDecimal remainingBalance;
    private Long atmId;

    public TransactionResponseDTO(String deposit, @NotNull @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0") BigDecimal summa, LocalDateTime timestamp, String description, BigDecimal balance) {
    }
}