import React from 'react';
import { GameState, Position, Wall } from '../types';

interface GameBoardProps {
  gameState: GameState;
  playerId: string;
  onMove: (pos: Position) => void;
  onPlaceWall: (wall: Wall) => void;
}

const CELL_SIZE = 50;
const PADDING = 20;

export const GameBoard: React.FC<GameBoardProps> = ({ gameState, playerId, onMove, onPlaceWall }) => {
  const { gridMinX, gridMaxX, gridMinY, gridMaxY, player1, player2, walls, currentTurnPlayerId } = gameState;

  const isMyTurn = currentTurnPlayerId === playerId;
  const me = player1.id === playerId ? player1 : player2;

  const boardWidth = 11 * CELL_SIZE + PADDING * 2;
  const boardHeight = 11 * CELL_SIZE + PADDING * 2;

  const handleCellClick = (x: number, y: number) => {
    if (!isMyTurn) return;
    onMove({ x, y });
  };

  const handleEdgeClick = (p1: Position, p2: Position) => {
    if (!isMyTurn || me?.wallsPlacedThisTurn! >= 3) return;
    onPlaceWall({ p1, p2 });
  };

  const getPos = (p: number) => p * CELL_SIZE + PADDING;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <div style={{ marginBottom: '1rem', color: 'var(--text-main)' }}>
        <h2>{isMyTurn ? "Your Turn" : "Opponent's Turn"}</h2>
        <p>Walls placed this turn: {me?.wallsPlacedThisTurn || 0} / 3</p>
      </div>
      <svg width={boardWidth} height={boardHeight} style={{ backgroundColor: 'var(--panel-bg)', borderRadius: '8px' }}>

        {/* Draw playable area boundary (shrinks) */}
        <rect
          x={getPos(gridMinX)}
          y={getPos(gridMinY)}
          width={(gridMaxX - gridMinX + 1) * CELL_SIZE}
          height={(gridMaxY - gridMinY + 1) * CELL_SIZE}
          fill="rgba(67, 48, 46, 0.05)"
          stroke="var(--accent)"
          strokeWidth="2"
        />

        {/* Draw cells */}
        {Array.from({ length: 11 }).map((_, x) =>
          Array.from({ length: 11 }).map((_, y) => {
            const isAdjacentParams = isMyTurn && me
              ? (Math.abs(me.position.x - x) + Math.abs(me.position.y - y) === 1)
              : false;

            // Check if blocked by wall (front-end visual check only)
            const isBlockedWalk = isAdjacentParams && walls.some(w =>
              (w.p1.x === me!.position.x && w.p1.y === me!.position.y && w.p2.x === x && w.p2.y === y) ||
              (w.p2.x === me!.position.x && w.p2.y === me!.position.y && w.p1.x === x && w.p1.y === y)
            );
            const canMoveTo = isAdjacentParams && !isBlockedWalk;

            return (
              <rect
                key={`cell-${x}-${y}`}
                x={getPos(x)}
                y={getPos(y)}
                width={CELL_SIZE}
                height={CELL_SIZE}
                fill="transparent"
                stroke="var(--grid-line)"
                strokeWidth="1"
                onClick={() => { if (canMoveTo) handleCellClick(x, y); }}
                style={{ cursor: canMoveTo ? 'pointer' : 'default', transition: '0.2s' }}
                onMouseEnter={(e) => { if (canMoveTo) e.currentTarget.style.fill = 'rgba(255, 255, 255, 0.1)' }}
                onMouseLeave={(e) => { if (canMoveTo) e.currentTarget.style.fill = 'transparent' }}
              />
            )
          })
        )}

        {/* Draw and handle Vertical Edges */}
        {Array.from({ length: 10 }).map((_, x) =>
          Array.from({ length: 11 }).map((_, y) => {
            const isPlaced = walls.some(w =>
              (w.p1.x === x && w.p1.y === y && w.p2.x === x + 1 && w.p2.y === y) ||
              (w.p2.x === x && w.p2.y === y && w.p1.x === x + 1 && w.p1.y === y)
            );
            const canPlace = isMyTurn && !isPlaced && (me?.wallsPlacedThisTurn || 0) < 3;
            return (
              <line
                key={`v-edge-${x}-${y}`}
                x1={getPos(x + 1)} y1={getPos(y)} x2={getPos(x + 1)} y2={getPos(y + 1)}
                stroke={isPlaced ? 'var(--wall-color)' : 'transparent'}
                strokeWidth={isPlaced ? 6 : 10}
                strokeLinecap="round"
                style={{ cursor: canPlace ? 'pointer' : 'default', pointerEvents: isPlaced ? 'none' : 'all' }}
                onClick={() => handleEdgeClick({ x, y }, { x: x + 1, y })}
                onMouseEnter={(e) => { if (canPlace) e.currentTarget.style.stroke = 'rgba(255,255,255,0.3)' }}
                onMouseLeave={(e) => { if (canPlace) e.currentTarget.style.stroke = 'transparent' }}
              />
            )
          })
        )}

        {/* Draw and handle Horizontal Edges */}
        {Array.from({ length: 11 }).map((_, x) =>
          Array.from({ length: 10 }).map((_, y) => {
            const isPlaced = walls.some(w =>
              (w.p1.x === x && w.p1.y === y && w.p2.x === x && w.p2.y === y + 1) ||
              (w.p2.x === x && w.p2.y === y && w.p1.x === x && w.p1.y === y + 1)
            );
            const canPlace = isMyTurn && !isPlaced && (me?.wallsPlacedThisTurn || 0) < 3;
            return (
              <line
                key={`h-edge-${x}-${y}`}
                x1={getPos(x)} y1={getPos(y + 1)} x2={getPos(x + 1)} y2={getPos(y + 1)}
                stroke={isPlaced ? 'var(--wall-color)' : 'transparent'}
                strokeWidth={isPlaced ? 6 : 10}
                strokeLinecap="round"
                style={{ cursor: canPlace ? 'pointer' : 'default', pointerEvents: isPlaced ? 'none' : 'all' }}
                onClick={() => handleEdgeClick({ x, y }, { x, y: y + 1 })}
                onMouseEnter={(e) => { if (canPlace) e.currentTarget.style.stroke = 'rgba(255,255,255,0.3)' }}
                onMouseLeave={(e) => { if (canPlace) e.currentTarget.style.stroke = 'transparent' }}
              />
            )
          })
        )}

        {/* Draw Players */}
        {player1.alive && (
          <image
            href="/player1.png"
            x={getPos(player1.position.x) + CELL_SIZE / 2 - 20}
            y={getPos(player1.position.y) + CELL_SIZE / 2 - 20}
            width={40}
            height={40}
            style={{ transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)' }}
          />
        )}
        {player2?.alive && (
          <image
            href="/player2.png"
            x={getPos(player2.position.x) + CELL_SIZE / 2 - 20}
            y={getPos(player2.position.y) + CELL_SIZE / 2 - 20}
            width={40}
            height={40}
            style={{ transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)' }}
          />
        )}
      </svg>
    </div>
  );
};
