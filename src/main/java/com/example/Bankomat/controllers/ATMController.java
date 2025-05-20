package com.example.Bankomat.controllers;

import com.example.Bankomat.DTO.CardOperationDTO;
import com.example.Bankomat.DTO.TransactionResponseDTO;
import com.example.Bankomat.services.ATMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atm")
@RequiredArgsConstructor
@Tag(name = "ATM Operations", description = "Операции с банкоматом: пополнение и снятие средств")
public class ATMController {
    private final ATMService atmService;

    @Operation(
            summary = "Пополнение счета",
            description = "Внесение денег на счет через банкомат"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное пополнение"),
            @ApiResponse(responseCode = "400", description = "Неверные данные (например, карта не найдена)")
    })
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> deposit(
            @RequestBody @Valid CardOperationDTO request
    ) {
        return ResponseEntity.ok(atmService.deposit(request));
    }

    @Operation(
            summary = "Снятие денег",
            description = "Снятие денег со счета через банкомат"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное снятие"),
            @ApiResponse(responseCode = "400", description = "Ошибка (недостаточно средств, банкомат пуст и т.д.)")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> withdraw(
            @RequestBody @Valid CardOperationDTO request
    ) {
        return ResponseEntity.ok(atmService.withdraw(request));
    }
}