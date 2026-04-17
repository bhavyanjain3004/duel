package com.duel.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String matchId;

    @Column(nullable = false)
    private String player1Id;

    private String player2Id;

    private String winnerId;

    @Column(nullable = false)
    private int totalTurns;

    @Column(nullable = false)
    private LocalDateTime endedAt;
}
