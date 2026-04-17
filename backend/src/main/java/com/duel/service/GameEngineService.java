package com.duel.service;

import com.duel.domain.GameState;
import com.duel.domain.PlayerState;
import com.duel.domain.Position;
import com.duel.domain.Wall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class GameEngineService {

    public boolean placeWall(GameState state, Wall newWall) {
        // Check bounds
        if (!isWallWithinBounds(state, newWall)) {
            log.warn("Wall rejected: out of bounds {}", newWall);
            return false;
        }

        // Check if wall is between adjacent cells
        if (!newWall.getP1().isAdjacentTo(newWall.getP2())) {
            log.warn("Wall rejected: not adjacent cells {}", newWall);
            return false;
        }

        // Check for duplicates
        newWall.normalize();
        for (Wall w : state.getWalls()) {
            Wall cw = new Wall(w.getP1(), w.getP2());
            cw.normalize();
            if (cw.equals(newWall)) {
                log.warn("Wall rejected: duplicate location {}", newWall);
                return false;
            }
        }

        // Apply temporarily
        state.getWalls().add(newWall);

        // BFS path check
        boolean isValid = hasPathBetweenPlayers(state);

        if (!isValid) {
            // Revert
            state.getWalls().remove(newWall);
            log.warn("Wall rejected: algorithm determined player isolation {}", newWall);
            return false;
        }

        log.info("Wall successfully placed at {}", newWall);
        return true;
    }

    private boolean isWallWithinBounds(GameState state, Wall wall) {
        return checkPosBounds(state, wall.getP1()) && checkPosBounds(state, wall.getP2());
    }

    private boolean checkPosBounds(GameState state, Position p) {
        return p.getX() >= state.getGridMinX() && p.getX() <= state.getGridMaxX() &&
               p.getY() >= state.getGridMinY() && p.getY() <= state.getGridMaxY();
    }

    public boolean movePlayer(GameState state, String playerId, Position newPos) {
        PlayerState player = state.getPlayer1().getId().equals(playerId) ? state.getPlayer1() : state.getPlayer2();
        if (player == null || !player.isAlive()) return false;

        Position current = player.getPosition();
        if (!current.isAdjacentTo(newPos)) return false;
        if (!checkPosBounds(state, newPos)) return false;

        // Check if wall blocks movement
        Wall moveWall = new Wall(current, newPos);
        moveWall.normalize();
        for (Wall w : state.getWalls()) {
            Wall cw = new Wall(w.getP1(), w.getP2());
            cw.normalize();
            if (cw.equals(moveWall)) return false;
        }

        // Valid move
        player.setPosition(newPos);
        return true;
    }

    private boolean hasPathBetweenPlayers(GameState state) {
        Position start = state.getPlayer1().getPosition();
        Position target = state.getPlayer2().getPosition();

        Set<Position> visited = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position curr = queue.poll();

            if (curr.getX() == target.getX() && curr.getY() == target.getY()) {
                return true;
            }

            for (Position adj : getValidAdjacentEmpty(state, curr)) {
                if (!visited.contains(adj)) {
                    visited.add(adj);
                    queue.add(adj);
                }
            }
        }
        return false;
    }

    private List<Position> getValidAdjacentEmpty(GameState state, Position p) {
        List<Position> result = new ArrayList<>();
        int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};

        for (int[] d : dirs) {
            Position next = new Position(p.getX() + d[0], p.getY() + d[1]);
            if (checkPosBounds(state, next)) {
                Wall wall = new Wall(p, next);
                wall.normalize();
                boolean blocked = false;
                for (Wall w : state.getWalls()) {
                    Wall cw = new Wall(w.getP1(), w.getP2());
                    cw.normalize();
                    if (cw.equals(wall)) {
                        blocked = true;
                        break;
                    }
                }
                if (!blocked) {
                    result.add(next);
                }
            }
        }
        return result;
    }

    public void shrinkGrid(GameState state) {
        state.setGridMinX(state.getGridMinX() + 1);
        state.setGridMaxX(state.getGridMaxX() - 1);
        state.setGridMinY(state.getGridMinY() + 1);
        state.setGridMaxY(state.getGridMaxY() - 1);

        // Check if either player is outside
        checkOutsideAndKill(state, state.getPlayer1());
        checkOutsideAndKill(state, state.getPlayer2());

        // Remove walls that are strictly outside
        state.getWalls().removeIf(w -> 
            !checkPosBounds(state, w.getP1()) && !checkPosBounds(state, w.getP2())
        );
    }

    private void checkOutsideAndKill(GameState state, PlayerState p) {
        if (!checkPosBounds(state, p.getPosition())) {
            p.setAlive(false);
        }
    }
}
