A Java-based server for online multiplayer Connect Four with built-in chat, user accounts, and a JavaFX admin UI. The server supports multiple concurrent games, synchronizes moves in real time, and tracks each player’s wins and losses.

Features

1-Multiplayer at scale: Host multiple matches simultaneously with thread-safe game sessions.

2-Real-time sync: Concurrency controls ensure smooth, consistent board updates.

3-Chat system: Players can message each other during matches.

4-Accounts & stats: Login/Signup; server records per-user wins/losses.

5-Admin UI (JavaFX): View online players, active games, recent moves, chats, and W/L stats.

Tech Stack

Language: Java

UI: JavaFX (server dashboard)

Networking: Java sockets (multithreaded)

Data model: Per-match game state; per-user profiles and statistics

How It Works

Server core: Listens for client connections, authenticates users, pairs opponents, validates moves, broadcasts state, and logs results.

Concurrency: A dedicated handler thread per client; synchronized game rooms prevent race conditions.

UI: JavaFX dashboard for monitoring players, games, chat logs, and outcomes.

Actual server:

<img width="599" height="429" alt="Screenshot 2025-08-29 at 10 29 34 PM" src="https://github.com/user-attachments/assets/a989d19d-07e8-4d67-94d6-c87fbbf64301" />
