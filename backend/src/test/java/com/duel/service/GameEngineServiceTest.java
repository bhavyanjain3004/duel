package com.duel.service;

import com.duel.domain.GameState;
import com.duel.domain.PlayerState;
import com.duel.domain.Position;
import com.duel.domain.Wall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineServiceTest {

    private GameEngineService gameEngineService;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        gameEngineService = new GameEngineService();
        gameState = new GameState();
        gameState.setGridMinX(0);
        gameState.setGridMaxX(10);
        gameState.setGridMinY(0);
        gameState.setGridMaxY(10);

        PlayerState p1 = new PlayerState("p1", new Position(0, 5), true, 0);
        PlayerState p2 = new PlayerState("p2", new Position(10, 5), true, 0);
        gameState.setPlayer1(p1);
        gameState.setPlayer2(p2);
    }

    @Test
    void testOpenGridPathExists() {
        // Just place a single random wall that doesn't block the grid
        Wall wall = new Wall(new Position(5, 5), new Position(5, 6));
        
        boolean result = gameEngineService.placeWall(gameState, wall);
        
        assertTrue(result, "Wall should be successfully placed on an open grid");
        assertEquals(1, gameState.getWalls().size(), "One wall should be added");
    }

    @Test
    void testFullyBlockedGridPathDoesNotExist() {
        // Surround Player 1 entirely: (0,5). Since grid limits X=0, we only need to block:
        // (0,5)->(1,5)
        // (0,5)->(0,6)
        // (0,5)->(0,4)
        
        gameEngineService.placeWall(gameState, new Wall(new Position(0, 5), new Position(1, 5)));
        gameEngineService.placeWall(gameState, new Wall(new Position(0, 5), new Position(0, 6)));
        
        // The last wall should fail because it fully isolates Player 1
        Wall fatalWall = new Wall(new Position(0, 5), new Position(0, 4));
        boolean result = gameEngineService.placeWall(gameState, fatalWall);
        
        assertFalse(result, "The wall closing the box should be rejected by BFS");
        assertEquals(2, gameState.getWalls().size(), "Fatal wall must not be saved to state");
    }

    @Test
    void testSingleCorridorPathExists() {
        // Create a massive vertical wall spanning almost the whole height of the board at X=5.
        // Leaves exactly ONE single gap at Y=10 for players to path through.
        for (int y = 0; y <= 9; y++) {
            boolean res = gameEngineService.placeWall(gameState, new Wall(new Position(4, y), new Position(5, y)));
            assertTrue(res, "Corridor wall at Y=" + y + " should be successfully placed");
        }
        
        // Prove that Player 1 can still reach Player 2 through the snake corridor gap at Y=10
        // By successfully placing another innocuous wall somewhere else
        Wall benignWall = new Wall(new Position(1, 1), new Position(1, 2));
        boolean result = gameEngineService.placeWall(gameState, benignWall);
        
        assertTrue(result, "BFS must successfully find the extremely complex snake path to Player 2");
    }
}
