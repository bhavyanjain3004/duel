package com.duel.controller;

import com.duel.domain.GameState;
import com.duel.domain.PlayerState;
import com.duel.domain.Position;
import com.duel.domain.Wall;
import com.duel.domain.MatchRecord;
import com.duel.repository.MatchRecordRepository;
import com.duel.service.GameEngineService;
import com.duel.service.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Controller
public class GameWebSocketController {

    private final MatchService matchService;
    private final GameEngineService gameEngine;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchRecordRepository matchRecordRepository;

    public GameWebSocketController(MatchService matchService, GameEngineService gameEngine, SimpMessagingTemplate messagingTemplate, MatchRecordRepository matchRecordRepository) {
        this.matchService = matchService;
        this.gameEngine = gameEngine;
        this.messagingTemplate = messagingTemplate;
        this.matchRecordRepository = matchRecordRepository;
    }

    @MessageMapping("/game.wall")
    @SuppressWarnings("unchecked")
    public void placeWall(@Payload Map<String, Object> payload) {
        String matchId = (String) payload.get("matchId");
        String playerId = (String) payload.get("playerId");
        Map<String, Number> p1 = (Map<String, Number>) ((Map<String, Object>) payload.get("wall")).get("p1");
        Map<String, Number> p2 = (Map<String, Number>) ((Map<String, Object>) payload.get("wall")).get("p2");

        GameState state = matchService.getMatch(matchId);
        if (state == null || !"IN_PROGRESS".equals(state.getStatus())) return;
        if (!state.getCurrentTurnPlayerId().equals(playerId)) return;

        PlayerState player = state.getPlayer1().getId().equals(playerId) ? state.getPlayer1() : state.getPlayer2();
        if (player.getWallsPlacedThisTurn() >= 3) {
            log.warn("Player {} attempted to place >3 walls.", playerId);
            return;
        }

        Wall wall = new Wall(new Position(p1.get("x").intValue(), p1.get("y").intValue()), new Position(p2.get("x").intValue(), p2.get("y").intValue()));
        log.info("Player {} attempting to place wall {}", playerId, wall);
        
        if (gameEngine.placeWall(state, wall)) {
            player.setWallsPlacedThisTurn(player.getWallsPlacedThisTurn() + 1);

            // Check if the wall just trapped the opponent (no valid moves after wall placement)
            String opponentId = state.getPlayer1().getId().equals(playerId)
                    ? state.getPlayer2().getId() : state.getPlayer1().getId();
            if (!hasValidMoves(state, opponentId)) {
                finishGame(state, playerId);
            }

            matchService.save(state);
            messagingTemplate.convertAndSend("/topic/game/" + matchId, state);
        } else {
            // Signal invalid
            messagingTemplate.convertAndSendToUser(playerId, "/topic/game/error", "Invalid wall");
        }
    }

    @MessageMapping("/game.move")
    @SuppressWarnings("unchecked")
    public void movePlayer(@Payload Map<String, Object> payload) {
        String matchId = (String) payload.get("matchId");
        String playerId = (String) payload.get("playerId");
        Map<String, Number> pos = (Map<String, Number>) payload.get("position");

        GameState state = matchService.getMatch(matchId);
        if (state == null || !"IN_PROGRESS".equals(state.getStatus())) return;
        if (!state.getCurrentTurnPlayerId().equals(playerId)) return;

        Position newPos = new Position(pos.get("x").intValue(), pos.get("y").intValue());
        
        if (gameEngine.movePlayer(state, playerId, newPos)) {
            // End of turn logic
            PlayerState player = state.getPlayer1().getId().equals(playerId) ? state.getPlayer1() : state.getPlayer2();
            player.setWallsPlacedThisTurn(0);
            state.setCurrentTurnPlayerId(state.getPlayer1().getId().equals(playerId) ? state.getPlayer2().getId() : state.getPlayer1().getId());
            state.setTurnCount(state.getTurnCount() + 1);

            if (state.getTurnCount() > 0 && state.getTurnCount() % 4 == 0) {
                // Every 2 rounds (4 turns: P1, P2, P1, P2) shrink the grid
                gameEngine.shrinkGrid(state);
            }

            // Win condition: a player was killed by grid shrink, OR the next player has no valid moves
            if (!state.getPlayer1().isAlive() || !state.getPlayer2().isAlive()) {
                // Grid shrink killed someone
                String winnerId;
                if (state.getPlayer1().isAlive() && !state.getPlayer2().isAlive()) {
                    winnerId = state.getPlayer1().getId();
                } else if (!state.getPlayer1().isAlive() && state.getPlayer2().isAlive()) {
                    winnerId = state.getPlayer2().getId();
                } else {
                    winnerId = "TIE_OR_STARVED";
                }
                finishGame(state, winnerId);
            } else if (!hasValidMoves(state, state.getCurrentTurnPlayerId())) {
                // Next player to move is completely trapped — the player who just moved wins
                finishGame(state, playerId);
            }

            matchService.save(state);
            messagingTemplate.convertAndSend("/topic/game/" + matchId, state);
        } else {
            messagingTemplate.convertAndSendToUser(playerId, "/topic/game/error", "Invalid move");
        }
    }

    private boolean hasValidMoves(GameState state, String playerId) {
        PlayerState p = state.getPlayer1().getId().equals(playerId) ? state.getPlayer1() : state.getPlayer2();
        if (p == null || !p.isAlive()) return false;
        Position pos = p.getPosition();
        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] d : dirs) {
            Position next = new Position(pos.getX() + d[0], pos.getY() + d[1]);
            // Must be within the current (possibly shrunk) grid bounds
            if (next.getX() < state.getGridMinX() || next.getX() > state.getGridMaxX() ||
                next.getY() < state.getGridMinY() || next.getY() > state.getGridMaxY()) {
                continue;
            }
            // Must not be blocked by a wall
            Wall moveWall = new Wall(pos, next);
            moveWall.normalize();
            boolean blocked = false;
            for (Wall w : state.getWalls()) {
                Wall cw = new Wall(w.getP1(), w.getP2());
                cw.normalize();
                if (cw.equals(moveWall)) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) return true;
        }
        return false;
    }

    private void finishGame(GameState state, String winnerId) {
        if ("FINISHED".equals(state.getStatus())) return; // already ended
        state.setStatus("FINISHED");
        state.setWinnerId(winnerId);
        log.info("Match {} finished. Turns: {}, Winner: {}", state.getMatchId(), state.getTurnCount(), winnerId);
        try {
            MatchRecord record = new MatchRecord();
            record.setMatchId(state.getMatchId());
            record.setPlayer1Id(state.getPlayer1().getId());
            record.setPlayer2Id(state.getPlayer2().getId());
            record.setWinnerId(winnerId);
            record.setTotalTurns(state.getTurnCount());
            record.setEndedAt(LocalDateTime.now());
            matchRecordRepository.save(record);
            log.info("Persisted MatchRecord to PostgreSQL for match {}", state.getMatchId());
        } catch (Exception e) {
            log.error("Failed to persist MatchRecord for match {}: {}", state.getMatchId(), e.getMessage());
        }
    }
}
