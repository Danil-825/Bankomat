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
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ATMService {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final ATMRepository atmRepository;
    private final TransactionRepository transactionRepository;


    public TransactionResponseDTO deposit(CardOperationDTO request) {
        Card card = cardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new RuntimeException("Card not found!"));

        Account account = card.getAccount();
        ATM atm = atmRepository.findById(request.getAtmId())
                .orElseThrow(() -> new RuntimeException("ATM not found!"));

        if (!"ACTIVE".equals(atm.getStatus())) {
            throw new RuntimeException("ATM is not active");
        }

        account.setBalance(account.getBalance().add(request.getSumma()));
        atm.setCashBalance(atm.getCashBalance().add(request.getSumma()));

        Transaction transaction = new Transaction();
        transaction.setSumma(request.getSumma());
        transaction.setType("DEPOSIT");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription("Cash deposit to account " + account.getNumberAccount());
        transaction.setCard(card);
        transaction.setAtm(atm);

        transactionRepository.save(transaction);
        accountRepository.save(account);
        atmRepository.save(atm);

        return new TransactionResponseDTO(
                transaction.getType(),
                transaction.getSumma(),
                transaction.getTimestamp(),
                transaction.getDescription(),
                account.getBalance(),
                atm.getId()
        );
    }

    public TransactionResponseDTO withdraw(CardOperationDTO request) {
        Card card = cardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new RuntimeException("Card not found!"));

        Account account = card.getAccount();
        ATM atm = atmRepository.findById(request.getAtmId())
                .orElseThrow(() -> new RuntimeException("ATM not found!"));

        if (!"ACTIVE".equals(atm.getStatus())) {
            throw new RuntimeException("ATM is not active!");
        }

        log.info("Attempting to withdraw {} from account {}. Current balance: {}",
                request.getSumma(), account.getNumberAccount(), account.getBalance());

        if (account.getBalance().compareTo(request.getSumma()) < 0) {
            throw new RuntimeException(
                    "Insufficient funds! Balance: " + account.getBalance() +
                            ", Requested: " + request.getSumma()
            );
        }

        if (atm.getCashBalance().compareTo(request.getSumma()) < 0) {
            throw new RuntimeException("ATM out of cash!");
        }

        account.setBalance(account.getBalance().subtract(request.getSumma()));
        atm.setCashBalance(atm.getCashBalance().subtract(request.getSumma()));

        Transaction transaction = new Transaction();
        transaction.setSumma(request.getSumma());
        transaction.setType("WITHDRAW");
        transaction.setTimestamp(LocalDateTime.now());
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
                account.getBalance(),
                atm.getId()
        );
    }
}