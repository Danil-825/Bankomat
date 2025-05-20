package com.example.Bankomat.repository;

import com.example.Bankomat.entity.ATM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ATMRepository extends JpaRepository<ATM, Long> {
    Optional<ATM> findFirstByStatus(String status);
}
