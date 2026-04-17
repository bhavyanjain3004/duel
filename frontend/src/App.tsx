import { useState, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import { Matchmaking } from './components/Matchmaking'
import { GameBoard } from './components/GameBoard'
import { GameState, Position, Wall } from './types'

function App() {
  const [gameState, setGameState] = useState<GameState | null>(null);
  const [playerId, setPlayerId] = useState<string | null>(null);
  const stompClient = useRef<Client | null>(null);

  const connectWebSocket = (matchId: string) => {
    const client = new Client({
      brokerURL: `${import.meta.env.VITE_WS_URL}/ws`,
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/game/${matchId}`, (message: any) => {
          setGameState(JSON.parse(message.body));
        });
      }
    });
    client.activate();
    stompClient.current = client;
  };

  const handleCreate = async (id: string) => {
    try {
      const res = await fetch(`${import.meta.env.VITE_BACKEND_URL}/api/match/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ playerId: id })
      });
      const state = await res.json();
      setPlayerId(id);
      setGameState(state);
      connectWebSocket(state.matchId);
    } catch (e) {
      console.error(e);
    }
  };

  const handleJoin = async (matchId: string, id: string) => {
    try {
      const res = await fetch(`${import.meta.env.VITE_BACKEND_URL}/api/match/join`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ matchId, playerId: id })
      });
      if (!res.ok) throw new Error("Could not join match");
      const state = await res.json();
      setPlayerId(id);
      setGameState(state);
      connectWebSocket(state.matchId);
    } catch (e) {
      console.error(e);
      alert("Failed to join match");
    }
  };

  const handleMove = (pos: Position) => {
    if (stompClient.current && stompClient.current.connected && gameState) {
      stompClient.current.publish({
        destination: `/app/game.move`,
        body: JSON.stringify({ matchId: gameState.matchId, playerId, position: pos })
      });
    }
  };

  const handlePlaceWall = (wall: Wall) => {
    if (stompClient.current && stompClient.current.connected && gameState) {
      stompClient.current.publish({
        destination: `/app/game.wall`,
        body: JSON.stringify({ matchId: gameState.matchId, playerId, wall })
      });
    }
  };

  return (
    <div className="app-container">
      <header className="header">
        <h1>Pathfinder Duel</h1>
        {gameState && <div style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Room Code: <span style={{ color: 'white', fontWeight: 'bold' }}>{gameState.matchId}</span></div>}
      </header>
      <main className="main-content">
        {!gameState ? (
          <Matchmaking onCreate={handleCreate} onJoin={handleJoin} />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
             {gameState.status === 'WAITING' && (
               <div style={{ textAlign: 'center', padding: '2rem', backgroundColor: 'var(--panel-bg)', borderRadius: '8px' }}>
                 <h2>Waiting for opponent...</h2>
                 <p>Share Room Code: <strong>{gameState.matchId}</strong></p>
               </div>
             )}
             {gameState.status === 'IN_PROGRESS' && (
               <GameBoard 
                  gameState={gameState} 
                  playerId={playerId!} 
                  onMove={handleMove} 
                  onPlaceWall={handlePlaceWall} 
               />
             )}
             {gameState.status === 'FINISHED' && (
               <div style={{ textAlign: 'center', padding: '2rem', backgroundColor: 'var(--panel-bg)', borderRadius: '8px', color: gameState.winnerId === playerId ? 'var(--player2-color)' : 'var(--player1-color)' }}>
                 <h2>Game Over</h2>
                 <h1>{gameState.winnerId === playerId ? 'You Win!' : 'You Lose!'}</h1>
                 {gameState.winnerId === 'TIE_OR_STARVED' && <h1>It's a Tie!</h1>}
                 <button onClick={() => window.location.reload()} style={{ padding: '1rem', marginTop: '1rem', cursor: 'pointer' }}>Play Again</button>
               </div>
             )}
          </div>
        )}
      </main>
    </div>
  )
}

export default App
