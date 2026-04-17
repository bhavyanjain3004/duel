# ⚔️ Pathfinder Duel

A real-time, 2-player strategy game where you trap your opponent before the shrinking arena traps you first.

> Built with Spring Boot · React · WebSockets · Redis · PostgreSQL

---

## 🎮 How to Play

Two players share a room code and face off on an **11×11 grid** that slowly shrinks around them.

**On your turn, you can:**
- 🚶 **Move** — step to any adjacent cell (up, down, left, right)
- 🧱 **Place walls** — block edges between cells (up to 3 walls per turn)

**You win if:**
- Your opponent has **no valid moves left** (fully surrounded)
- Your opponent gets **caught outside the grid** when it shrinks

**Walls can't completely isolate a player** — the engine runs a BFS path check and rejects any wall that would do that. So you'll have to be clever about it.

Every **2 rounds**, the playable area shrinks by 1 on all sides. The pressure builds fast.

---

## 🏗️ Tech Stack

| Layer | Tech |
|-------|------|
| Frontend | React 18 + TypeScript + Vite |
| Real-time | WebSockets via STOMP (`@stomp/stompjs`) |
| Backend | Java 21 + Spring Boot 3 |
| Game State | Redis (fast in-memory, 2h TTL) |
| Match History | PostgreSQL (persistent telemetry) |
| CI | GitHub Actions |

---

## 🗂️ Project Structure

```
duel/
├── frontend/          # React/Vite app
│   ├── src/
│   │   ├── App.tsx            # Game orchestration + WebSocket client
│   │   ├── components/
│   │   │   ├── GameBoard.tsx  # SVG game board renderer
│   │   │   └── Matchmaking.tsx# Room create/join UI
│   │   └── types.ts           # Shared TypeScript types
│   └── vercel.json            # Vercel deployment config
│
└── backend/           # Spring Boot app
    └── src/main/java/com/duel/
        ├── controller/
        │   ├── MatchController.java          # REST: create & join matches
        │   └── GameWebSocketController.java  # WS: move & wall events
        ├── service/
        │   ├── MatchService.java             # Redis match state management
        │   └── GameEngineService.java        # BFS, wall validation, win logic
        └── domain/                           # GameState, PlayerState, Wall, Position
```

---

## 🚀 Running Locally

### Prerequisites
- Java 21, Maven (bundled in repo)
- Redis running on `localhost:6379`
- PostgreSQL running on `localhost:5432` with a `duel_db` database

### Quick Start

**1. Start the backend**
```bash
cd backend
./apache-maven-3.9.6/bin/mvn spring-boot:run
```

**2. Start the frontend** (in a new terminal)
```bash
cd frontend
npm install
npm run dev
```

**3. Open two browser tabs** at `http://localhost:3000`
- Tab 1: Click **Create New Game** → copy the room code
- Tab 2: Paste the code → click **Join Game**

### Environment Variables (Frontend)

Create `frontend/.env.local` for local development:
```
VITE_BACKEND_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080
```

---

## ⚙️ How the Engine Works

### Wall Validation
Every wall placement is tested against a **BFS connectivity check** before being accepted. If the wall would leave either player with no reachable path, it's rejected — keeping the game fair and solvable.

### Win Conditions
The game ends when:
1. A player is inside the grid but **all 4 adjacent cells are blocked** by walls or the grid boundary → the player who just moved (or placed the last wall) wins
2. A player's position falls **outside the shrunken grid boundary** → they die, opponent wins
3. Both die simultaneously → **Tie**

### Turn Structure
- Each turn: move **once**, optionally place **up to 3 walls** (before or after moving)
- After every 2 full rounds (4 turns total), the grid shrinks by 1 on each side
- Match results are persisted to PostgreSQL when the game ends

---

## 🔬 CI

GitHub Actions runs on every push to `main`:
- **build-backend**: Compiles the Spring Boot app with bundled Maven, runs JUnit tests
- **build-frontend**: Installs deps, runs TypeScript check (`tsc`), builds Vite bundle

---

## 📄 License

MIT — feel free to fork, remix, and build on it.
