package com.example.Bankomat.services;

import com.example.Bankomat.DTO.AuthRequest;
import com.example.Bankomat.DTO.AuthResponse;
import com.example.Bankomat.DTO.ClientDTO;
import com.example.Bankomat.config.JwtTokenProvider;
import com.example.Bankomat.entity.Account;
import com.example.Bankomat.entity.Card;
import com.example.Bankomat.entity.Client;
import com.example.Bankomat.entity.Staff;
import com.example.Bankomat.entity.enums.UserRole;
import com.example.Bankomat.repository.CardRepository;
import com.example.Bankomat.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final CardRepository cardRepository;
    private final StaffRepository staffRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse authenticateClient(AuthRequest request) {
        Card card = cardRepository.findByCardNumber(request.getLogin())
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        if (!request.getPassword().equals(card.getPinHash())) {
            throw new RuntimeException("Неверный PIN-код");
        }

        if (card.isBlocked()) {
            throw new RuntimeException("Карта заблокирована");
        }

        Account account = card.getAccount();
        Client client = account.getClient();
        ClientDTO clientDto = new ClientDTO(
                client.getSurname() + " " + client.getName(),
                account.getBalance(),
                maskCardNumber(card.getCardNumber())
        );

        String token = jwtTokenProvider.generateToken(
                card.getCardNumber(),
                UserRole.CLIENT.name()
        );

        return new AuthResponse(token, UserRole.CLIENT.name(), clientDto);
    }

    public AuthResponse authenticateStaff(AuthRequest request) {
        Staff staff = staffRepository.findByEmail(request.getLogin())
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        if (!request.getPassword().equals(staff.getPassword())) {
            throw new RuntimeException("Неверный пароль");
        }

        String token = jwtTokenProvider.generateToken(
                staff.getEmail(),
                staff.getRole().name()
        );

        return new AuthResponse(token, staff.getRole().name(), null);
    }

    private String maskCardNumber(String cardNumber) {
        return "•••• " + cardNumber.substring(cardNumber.length() - 4);
    }
}
