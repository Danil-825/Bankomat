package com.example.Bankomat.repository;

import com.example.Bankomat.entity.ATM;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ATMRepository extends JpaRepository<ATM, Long> {
}
