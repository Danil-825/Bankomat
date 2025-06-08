package com.example.Bankomat.services;

import com.example.Bankomat.DTO.CardOperationDTO;
import com.example.Bankomat.DTO.TransactionResponseDTO;
import com.example.Bankomat.entity.ATM;
import com.example.Bankomat.entity.Account;
import com.example.Bankomat.entity.Card;
import com.example.Bankomat.entity.Transaction;
import com.example.Bankomat.repository.ATMRepository;
import com.example.Bankomat.repository.AccountRepository;
import com.example.Bankomat.repository.CardRepository;
import com.example.Bankomat.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ATMService {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final ATMRepository atmRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;

    // Константы для лимитов
    private static final BigDecimal MIN_CASH_LIMIT = new BigDecimal("20000");
    private static final BigDecimal MAX_CASH_LIMIT = new BigDecimal("5000000");

    @Scheduled(fixedRate = 3600000)
    public void checkATMCashBalance() {
        List<ATM> allATMs = atmRepository.findAll();

        for (ATM atm : allATMs) {
            if (atm.getCashBalance().compareTo(MIN_CASH_LIMIT) < 0) {
                sendLowCashNotification(atm);
            } else if (atm.getCashBalance().compareTo(MAX_CASH_LIMIT) > 0) {
                sendExcessCashNotification(atm);
            }
        }
    }

    private void sendLowCashNotification(ATM atm) {
        String subject = "⚠️ Низкий остаток в банкомате #" + atm.getId();
        String message = String.format(
                "Банкомат %s (ID: %d) требует пополнения!\nТекущий остаток: %.2f руб.\nМинимальный лимит: %.2f руб.",
                atm.getLocation(), atm.getId(), atm.getCashBalance(), MIN_CASH_LIMIT
        );

        emailService.sendAdminNotification(subject, message);
        log.warn(message);
    }

    private void sendExcessCashNotification(ATM atm) {
        String subject = "⚠️ Превышен лимит в банкомате #" + atm.getId();
        String message = String.format(
                "Банкомат %s (ID: %d) содержит слишком много наличных!\nТекущий остаток: %.2f руб.\nМаксимальный лимит: %.2f руб.",
                atm.getLocation(), atm.getId(), atm.getCashBalance(), MAX_CASH_LIMIT
        );

        emailService.sendAdminNotification(subject, message);
        log.warn(message);
    }

    // Внесение денег
    public TransactionResponseDTO deposit(CardOperationDTO request) {
        Card card = cardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new RuntimeException("Card not found!"));

        Account account = card.getAccount();
        ATM atm = atmRepository.findById(request.getAtmId())
                .orElseThrow(() -> new RuntimeException("ATM not found!"));
        if (!"ACTIVE".equals(atm.getStatus())) {
            throw new RuntimeException("ATM is not active!");
        }

        // Обновляем балансы
        account.setBalance(account.getBalance().add(request.getSumma()));
        atm.setCashBalance(atm.getCashBalance().add(request.getSumma()));

        // Создаем транзакцию
        Transaction transaction = new Transaction();
        transaction.setSumma(request.getSumma());
        transaction.setType("DEPOSIT");
        transaction.setDescription("Cash deposit to account " + account.getNumberAccount());
        transaction.setCard(card);
        transaction.setAtm(atm);

        transactionRepository.save(transaction);
        accountRepository.save(account);
        atmRepository.save(atm);

        return new TransactionResponseDTO(
                "DEPOSIT",
                request.getSumma(),
                transaction.getTimestamp(),
                transaction.getDescription(),
                account.getBalance()
        );
    }

    // Снятие денег
    public TransactionResponseDTO withdraw(CardOperationDTO request) {
        Card card = cardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new RuntimeException("Card not found!"));

        Account account = card.getAccount();
        ATM atm = atmRepository.findById(request.getAtmId())
                .orElseThrow(() -> new RuntimeException("ATM not found!"));

        if (!"ACTIVE".equals(atm.getStatus())) {
            throw new RuntimeException("ATM is not active!");
        }

        // Проверка баланса
        if (account.getBalance().compareTo(request.getSumma()) < 0) {
            throw new RuntimeException("Insufficient funds!");
        }
        if (atm.getCashBalance().compareTo(request.getSumma()) < 0) {
            throw new RuntimeException("ATM out of cash!");
        }

        // Обновляем балансы
        account.setBalance(account.getBalance().subtract(request.getSumma()));
        atm.setCashBalance(atm.getCashBalance().subtract(request.getSumma()));

        //Делаем переменную показывающую сегодняшнюю дату и время
        LocalDateTime now = LocalDateTime.now();

        // Создаем транзакцию
        Transaction transaction = new Transaction();
        transaction.setSumma(request.getSumma());
        transaction.setType("WITHDRAW");
        transaction.setTimestamp(now);
        transaction.setDescription("Cash withdrawal from account " + account.getNumberAccount());
        transaction.setCard(card);
        transaction.setAtm(atm);

        transactionRepository.save(transaction);
        accountRepository.save(account);
        atmRepository.save(atm);

        return new TransactionResponseDTO(
                "WITHDRAW",
                request.getSumma(),
                transaction.getTimestamp(),
                transaction.getDescription(),
                account.getBalance()
        );
    }
}