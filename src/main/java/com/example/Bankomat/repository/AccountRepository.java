package com.example.Bankomat.repository;

import com.example.Bankomat.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
