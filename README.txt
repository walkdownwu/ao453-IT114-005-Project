============================================================
  Networked Rock Paper Scissors
  IT 114 — Network Programming
  Author: Wu (ao453)
  GitHub: github.com/walkdownwu/ao453-IT114-005
============================================================

------------------------------------------------------------
  HOW TO COMPILE
------------------------------------------------------------

Prerequisites: Java JDK 11 or higher installed, with javac on your PATH.

From the project root directory (where /src and /bin are):

  Mac / Linux / Git Bash:
    javac -d bin $(find src -name "*.java")

  Windows CMD:
    javac -d bin src/rps/common/*.java src/rps/server/*.java src/rps/client/views/*.java src/rps/client/*.java

If the bin/ directory does not exist, create it first:
    mkdir bin


------------------------------------------------------------
  HOW TO START THE SERVER
------------------------------------------------------------

After compiling, run from the project root:

    java -cp bin rps.server.Server

The server starts on port 3001 by default.
To use a different port:

    java -cp bin rps.server.Server 5000

The server prints "Server ready." when it is accepting connections.
The leaderboard is automatically loaded from leaderboard.dat if it exists.


------------------------------------------------------------
  HOW TO CONNECT CLIENTS
------------------------------------------------------------

Open a new terminal window for each client and run:

    java -cp bin rps.client.ClientUI

A GUI window will appear. Enter:
  - Username   (any non-empty name)
  - Host       (use "localhost" if server is on the same machine)
  - Port       (must match the port the server started on; default 3001)

Click "Connect". You will be taken to the Lobby.


------------------------------------------------------------
  HOW TO PLAY
------------------------------------------------------------

FROM THE LOBBY:
  - "Create Public Room"  — creates a room anyone can see and join
  - "Create Private Room" — creates a hidden room; share the name with friends
  - "Join Public Room"    — select a room from the list and click this button
  - "Join Private Room"   — enter the room name manually when prompted

IN A GAME ROOM (Ready Screen):
  - Click "Ready Up" when you are ready to play
  - The game starts automatically once ALL players (minimum 2) are ready
  - Click "Leave Room" to return to the lobby

IN THE GAME:
  - Click ✊ Rock, ✋ Paper, or ✌ Scissors to make your choice
  - You have 15 seconds — if time runs out a random choice is assigned
  - Round results and eliminations are shown in the events log
  - The "Return to Lobby" button appears after the game ends
  - ALL players must click "Return to Lobby" for the ready screen to reset

LEADERBOARD:
  - Visible in the Lobby panel on the right side
  - Shows all-time wins per player, sorted highest first
  - Persists across server restarts (saved in leaderboard.dat)
  - Updates live after every completed game


------------------------------------------------------------
  PROJECT STRUCTURE
------------------------------------------------------------

rps/
├── README.txt               ← this file
├── leaderboard.dat          ← created automatically after first win
├── src/
│   └── rps/
│       ├── common/          ← shared between client and server
│       │   ├── Constants.java
│       │   ├── Payload.java
│       │   ├── PointsPayload.java
│       │   ├── RoomPayload.java
│       │   ├── LeaderboardPayload.java
│       │   ├── PayloadType.java
│       │   └── Phase.java
│       ├── server/
│       │   ├── Server.java        ← entry point, accept loop, room management
│       │   ├── ServerThread.java  ← one per connected client
│       │   ├── Room.java          ← base room (lobby)
│       │   ├── GameRoom.java      ← game logic, round resolution
│       │   ├── Player.java        ← per-player state
│       │   └── Leaderboard.java   ← persistent win tracking
│       └── client/
│           ├── Client.java        ← networking, payload dispatch
│           ├── ClientUI.java      ← main Swing frame, IClientEvents impl
│           ├── IClientEvents.java ← callback interface
│           └── views/
│               ├── ConnectionPanel.java  ← login screen
│               ├── LobbyPanel.java       ← room list + leaderboard
│               ├── ReadyPanel.java       ← waiting room / ready check
│               ├── GamePanel.java        ← in-game choices + events
│               └── UserListPanel.java    ← player list with points
└── bin/                     ← compiled .class files (generated)


------------------------------------------------------------
  DELIVERABLES
------------------------------------------------------------

1. MATCHMAKING & GAME ROOMS
   Public rooms are created by any player and visible to all in the
   Lobby table. Private rooms are hidden from the list and require the
   exact room name to join.

2. GUI WITH ICONS
   The game panel uses ✊ ✋ ✌ emoji icon buttons for Rock, Paper,
   Scissors. Buttons highlight green on selection and grey out the
   remaining options. Phase labels update dynamically throughout the game.

3. PERSISTENT LEADERBOARD
   All-time wins are saved to leaderboard.dat on the server after every
   completed game. The file reloads on server restart. All clients in the
   lobby receive live leaderboard updates after each game.


------------------------------------------------------------
  ERROR HANDLING NOTES
------------------------------------------------------------

- If a client disconnects mid-game, the server catches the IOException,
  removes the player cleanly, and notifies remaining players.
- Invalid or null payloads are caught and logged without crashing the server.
- Invalid choice values (anything other than r/p/s) are rejected server-side.
- If the timer expires before all players choose, random choices are auto-assigned.
- The server accept loop catches individual connection failures so the server
  stays running even if one client fails to connect properly.
============================================================
