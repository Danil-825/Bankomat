package com.example.Bankomat.repository;

import com.example.Bankomat.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, String> {
    Optional<Card> findByCardNumber(String cardNumber);
}
