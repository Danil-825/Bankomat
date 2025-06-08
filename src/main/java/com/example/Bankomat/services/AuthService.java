package com.example.Bankomat.services;

import com.example.Bankomat.DTO.AuthRequest;
import com.example.Bankomat.DTO.AuthResponse;
import com.example.Bankomat.DTO.ClientDTO;
import com.example.Bankomat.config.JwtTokenProvider;
import com.example.Bankomat.entity.*;
import com.example.Bankomat.entity.enums.UserRole;
import com.example.Bankomat.repository.CardRepository;
import com.example.Bankomat.repository.StaffRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final CardRepository cardRepository;
    private final StaffRepository staffRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;

    public AuthResponse authenticateClient(AuthRequest request) {
        String cardNumber = request.getLogin();

        // Проверка блокировки карты
        if (loginAttemptService.isBlocked(cardNumber)) {
            sendBlockNotification(cardNumber);
            throw new RuntimeException("Карта временно заблокирована. Превышено количество попыток");
        }

        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> {
                    logSecurityEvent("Неудачная попытка входа (карта не найдена)", cardNumber);
                    return new RuntimeException("Карта не найдена");
                });

        if (!passwordEncoder.matches(request.getPassword(), card.getPinHash())) {
            loginAttemptService.loginFailed(cardNumber);
            int remainingAttempts = loginAttemptService.getRemainingAttempts(cardNumber);

            if (remainingAttempts <= 0) {
                sendBlockNotification(cardNumber);
                throw new RuntimeException("Карта заблокирована. Превышено количество попыток");
            }

            throw new RuntimeException("Неверный PIN-код. Осталось попыток: " + remainingAttempts);
        }

        // Успешная аутентификация
        loginAttemptService.loginSucceeded(cardNumber);

        if (card.isBlocked()) {
            throw new RuntimeException("Карта заблокирована администратором");
        }

        Account account = card.getAccount();
        Client client = account.getClient();

        return generateAuthResponse(
                card.getCardNumber(),
                UserRole.CLIENT,
                new ClientDTO(
                        client.getSurname() + " " + client.getName(),
                        account.getBalance(),
                        maskCardNumber(card.getCardNumber())
                )
        );
    }

    // Метод для персонала
    public AuthResponse authenticateStaff(AuthRequest request) {
        String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            String message = "Блокировка IP: " + ip + " (превышено количество попыток)";
            emailService.sendAdminNotification("Блокировка входа", message);
            throw new RuntimeException("Превышено количество попыток. Попробуйте позже");
        }

        Staff staff = staffRepository.findByEmail(request.getLogin())
                .orElseThrow(() -> {
                    handleFailedAttempt(ip, request.getLogin(), "Несуществующий пользователь");
                    return new RuntimeException("Неверные учетные данные");
                });

        if (!passwordEncoder.matches(request.getPassword(), staff.getPassword())) {
            handleFailedAttempt(ip, staff.getEmail(), "Неверный пароль");
            throw new RuntimeException("Неверные учетные данные");
        }

        loginAttemptService.loginSucceeded(ip);
        return generateAuthResponse(staff.getEmail(), staff.getRole(), null);
    }

    // Генерация AuthResponse
    private AuthResponse generateAuthResponse(String username, UserRole role, ClientDTO clientDTO) {
        String token = jwtTokenProvider.generateToken(username, role.name());
        return new AuthResponse(token, role.name(), clientDTO);
    }

    // Получение IP клиента
    private String getClientIP() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes())
                .getRequest();
        return request.getRemoteAddr();
    }

    // Обработка неудачных попыток
    private void handleFailedAttempt(String ip, String username, String reason) {
        loginAttemptService.loginFailed(ip);

        if (loginAttemptService.isBlocked(ip)) {
            String subject = "Подозрительная активность";
            String message = String.format(
                    "Обнаружена попытка взлома:\nIP: %s\nАккаунт: %s\nПричина: %s\nКоличество попыток: %d",
                    ip, username, reason, loginAttemptService.getAttempts(ip)
            );
            emailService.sendAdminNotification(subject, message);
        }
    }

    private void logSecurityEvent(String event, String details) {
        log.warn("Security Event: {} - {}", event, details);
    }

    private String maskCardNumber(String cardNumber) {
        return "•••• " + cardNumber.substring(cardNumber.length() - 4);
    }
    public void updateCardPin(String cardNumber, String newPlainPin) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        String hashedPin = passwordEncoder.encode(newPlainPin);
        card.setPinHash(hashedPin);
        cardRepository.save(card);
    }

    public int getRemainingAttempts(String cardNumber) {
        int attempts = loginAttemptService.getAttempts(cardNumber);
        return Math.max(0, 3 - attempts);
    }

    private void sendBlockNotification(String cardNumber) {
        try {
            Card card = cardRepository.findByCardNumber(cardNumber)
                    .orElseThrow(() -> new RuntimeException("Карта не найдена"));

            Client client = card.getAccount().getClient();
            if (client.getEmail() != null) {
                emailService.sendAccountLockedEmail(
                        client.getEmail(),
                        client.getName() + " " + client.getSurname()
                );
            }

            // Уведомление администратора
            emailService.sendAdminNotification(
                    "Блокировка карты",
                    "Карта " + maskCardNumber(cardNumber) + " заблокирована после 3 неудачных попыток"
            );
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления о блокировке", e);
        }
    }
}