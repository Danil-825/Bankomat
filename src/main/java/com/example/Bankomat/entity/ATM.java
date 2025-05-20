package com.example.Bankomat.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "atm")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ATM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String location;
    @Column(name = "cashbalance")
    private BigDecimal cashBalance;
    private String status;
    @Column(name = "lastmaintenance")
    private LocalDate lastMaintenance;

    @OneToMany(mappedBy = "atm")
    private List<Transaction> transactions;
    
}
