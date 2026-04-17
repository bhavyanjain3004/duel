package com.duel.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerState implements Serializable {
    private String id;
    private Position position;
    private boolean alive;
    private int wallsPlacedThisTurn;
}
