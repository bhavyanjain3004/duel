package com.duel.controller;

import com.duel.domain.GameState;
import com.duel.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;
    private final SimpMessagingTemplate messagingTemplate;

    public MatchController(MatchService matchService, SimpMessagingTemplate messagingTemplate) {
        this.matchService = matchService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<GameState> createMatch(@RequestBody Map<String, String> payload) {
        String playerId = payload.get("playerId");
        if (playerId == null || playerId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(matchService.createMatch(playerId));
    }

    @PostMapping("/join")
    public ResponseEntity<GameState> joinMatch(@RequestBody Map<String, String> payload) {
        String matchId = payload.get("matchId");
        String playerId = payload.get("playerId");
        if (matchId == null || playerId == null) {
            return ResponseEntity.badRequest().build();
        }
        GameState state = matchService.joinMatch(matchId, playerId);
        if (state == null) {
            return ResponseEntity.badRequest().build();
        }
        messagingTemplate.convertAndSend("/topic/game/" + matchId, state);
        return ResponseEntity.ok(state);
    }
}
