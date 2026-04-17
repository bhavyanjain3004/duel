export interface Position {
  x: number;
  y: number;
}

export interface Wall {
  p1: Position;
  p2: Position;
}

export interface PlayerState {
  id: string;
  position: Position;
  alive: boolean;
  wallsPlacedThisTurn: number;
}

export interface GameState {
  matchId: string;
  gridMinX: number;
  gridMaxX: number;
  gridMinY: number;
  gridMaxY: number;
  turnCount: number;
  currentTurnPlayerId: string;
  player1: PlayerState;
  player2: PlayerState | null;
  walls: Wall[];
  status: 'WAITING' | 'IN_PROGRESS' | 'FINISHED';
  winnerId: string | null;
}
