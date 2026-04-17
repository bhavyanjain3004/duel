package com.duel.service;

import com.duel.domain.GameState;
import com.duel.domain.PlayerState;
import com.duel.domain.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MatchService {

    private final RedisTemplate<String, GameState> redisTemplate;

    public MatchService(RedisTemplate<String, GameState> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public GameState createMatch(String playerId) {
        GameState state = new GameState();
        state.setMatchId(UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        
        PlayerState p1 = new PlayerState(playerId, new Position(0, 5), true, 0); // Middle left
        state.setPlayer1(p1);
        state.setCurrentTurnPlayerId(playerId);
        
        save(state);
        log.info("Created new match {} for player {}", state.getMatchId(), playerId);
        return state;
    }
    
    public GameState joinMatch(String matchId, String playerId) {
        GameState state = getMatch(matchId);
        if (state != null && state.getPlayer2() == null && !playerId.equals(state.getPlayer1().getId())) {
            PlayerState p2 = new PlayerState(playerId, new Position(10, 5), true, 0); // Middle right
            state.setPlayer2(p2);
            state.setStatus("IN_PROGRESS");
            save(state);
            log.info("Player {} successfully joined match {}", playerId, matchId);
            return state;
        }
        log.warn("Player {} failed to join match {}", playerId, matchId);
        return null;
    }

    public GameState getMatch(String matchId) {
        return redisTemplate.opsForValue().get("match:" + matchId);
    }

    public void save(GameState state) {
        redisTemplate.opsForValue().set("match:" + state.getMatchId(), state, 2, TimeUnit.HOURS);
    }
}
