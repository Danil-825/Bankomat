package com.example.Bankomat.repository;

import com.example.Bankomat.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
