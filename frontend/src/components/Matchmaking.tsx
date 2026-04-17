import React, { useState } from 'react';

interface MatchmakingProps {
  onJoin: (matchId: string, playerId: string) => void;
  onCreate: (playerId: string) => void;
}

export const Matchmaking: React.FC<MatchmakingProps> = ({ onJoin, onCreate }) => {
  const [playerId, setPlayerId] = useState(`Player-${Math.floor(Math.random() * 1000)}`);
  const [matchId, setMatchId] = useState('');

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', width: '300px', margin: '0 auto', padding: '2rem', backgroundColor: 'var(--panel-bg)', borderRadius: '12px', boxShadow: '0 8px 32px rgba(67, 48, 46, 0.15)' }}>
      <h2>Find a Match</h2>
      
      <div>
        <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Player ID</label>
        <input 
          value={playerId} 
          onChange={e => setPlayerId(e.target.value)}
          style={{ width: '100%', padding: '0.5rem', marginTop: '0.2rem', borderRadius: '4px', border: '1px solid var(--accent)', backgroundColor: 'var(--bg-color)', color: 'var(--text-main)' }}
        />
      </div>

      <button 
        onClick={() => onCreate(playerId)}
        style={{ padding: '0.75rem', backgroundColor: 'var(--accent)', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', marginTop: '1rem' }}
      >
        Create New Game
      </button>

      <div style={{ textAlign: 'center', margin: '1rem 0', color: 'var(--text-muted)' }}>OR</div>

      <div>
         <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Room Code</label>
         <input 
          value={matchId} 
          onChange={e => setMatchId(e.target.value.toUpperCase())}
          placeholder="e.g., A1B2C3"
          style={{ width: '100%', padding: '0.5rem', marginTop: '0.2rem', borderRadius: '4px', border: '1px solid var(--accent)', backgroundColor: 'var(--bg-color)', color: 'var(--text-main)' }}
        />
      </div>

      <button 
        onClick={() => onJoin(matchId, playerId)}
        style={{ padding: '0.75rem', backgroundColor: 'var(--accent)', color: 'var(--panel-bg)', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
      >
        Join Game
      </button>

    </div>
  );
};
