package com.example.Bankomat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "account")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "number_account")
    private String numberAccount;
    private BigDecimal balance;
    @Column(name = "isactive")
    private boolean isActive;

    @OneToMany(mappedBy = "account")
    private List<Card> cards;

}
