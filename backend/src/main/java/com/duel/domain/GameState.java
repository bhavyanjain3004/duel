package com.duel.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class GameState implements Serializable {
    private String matchId;
    private int gridMinX;
    private int gridMaxX;
    private int gridMinY;
    private int gridMaxY;
    private int turnCount;
    private String currentTurnPlayerId;
    
    private PlayerState player1;
    private PlayerState player2;
    
    private List<Wall> walls = new ArrayList<>();
    
    private String status; // WAITING, IN_PROGRESS, FINISHED
    private String winnerId;
    
    public GameState() {
        // Default 11x11 grid (0 to 10)
        gridMinX = 0;
        gridMaxX = 10;
        gridMinY = 0;
        gridMaxY = 10;
        turnCount = 0;
        status = "WAITING";
    }
}
