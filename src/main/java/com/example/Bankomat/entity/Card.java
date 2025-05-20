package com.example.Bankomat.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "card")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Id
    @Column(name = "cardnumber")
    private String cardNumber;
    @Column(name = "pinhash")
    private String pinHash;
    @Column(name = "expirydate")
    private LocalDate expiryDate;
    @Column(name = "isblocked")
    private boolean isBlocked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToMany(mappedBy = "card")
    private List<Transaction> transactions;
}
