package rps.server;

import java.util.*;
import java.util.stream.Collectors;
import rps.common.*;

public class GameRoom extends Room {
    private Map<Long, Player> players = new HashMap<>();
    private Phase currentPhase = Phase.WAITING;
    private long sessionCreator = -1;
    private int returnedCount = 0;

    public GameRoom(String name, boolean isPrivate) {
        super(name, isPrivate);
    }

    @Override
    protected void onClientAdded(ServerThread client) {
        super.onClientAdded(client);
        Player player = new Player(client.getClientId(), client.getClientName());
        players.put(client.getClientId(), player);
        if (sessionCreator == -1) sessionCreator = client.getClientId();
        syncAllPoints();
        if (currentPhase == Phase.WAITING) setPhase(Phase.READY_CHECK);
        Server.INSTANCE.broadcastRoomList();
    }

    @Override
    protected void onClientRemoved(ServerThread client) {
        players.remove(client.getClientId());
        super.onClientRemoved(client);
        if (players.isEmpty()) {
            setPhase(Phase.WAITING);
            sessionCreator = -1;
        }
        Server.INSTANCE.broadcastRoomList();
    }

    public void handleReady(ServerThread client, boolean isReady) {
        Player player = players.get(client.getClientId());
        if (player == null || player.isSpectator()) return;
        player.setReady(isReady);
        sendMessage(null, client.getClientName() + (isReady ? " is ready" : " is not ready"));

        boolean allReady = players.values().stream()
            .filter(p -> !p.isSpectator())
            .allMatch(Player::isReady);
        long activePlayers = players.values().stream().filter(p -> !p.isSpectator()).count();

        if (allReady && activePlayers >= 2) {
            startSession();
        }
    }

    public void handlePick(ServerThread client, String choice) {
        if (currentPhase != Phase.CHOOSING) return;
        Player player = players.get(client.getClientId());
        if (player == null || player.isEliminated() || player.getCurrentChoice() != null) return;
        player.setCurrentChoice(choice);
        sendMessage(null, client.getClientName() + " has made their choice");
        checkAllChosen();
    }

    public void handleAway(ServerThread client, boolean away) {
        Player player = players.get(client.getClientId());
        if (player == null) return;
        player.setAway(away);
        sendMessage(null, client.getClientName() + (away ? " is away" : " is back"));
    }

    public synchronized void handleReturnToLobby(ServerThread client) {
        Player player = players.get(client.getClientId());
        if (player == null || player.hasReturnedToLobby()) return;
        player.setReturnedToLobby(true);
        returnedCount++;
        long activeCount = players.values().stream().filter(p -> !p.isSpectator()).count();
        if (activeCount == 0 || returnedCount >= activeCount) {
            returnedCount = 0;
            for (Player p : players.values()) p.setReturnedToLobby(false);
            resetForNewGame();
            setPhase(Phase.READY_CHECK);
        }
    }

    private void startSession() {
        for (Player p : players.values()) p.reset();
        syncAllPoints();
        startRound();
    }

    private void startRound() {
        setPhase(Phase.CHOOSING);
        for (Player p : players.values()) p.resetRound();
        sendMessage(null, "Make your choice!");
        startTimer();
    }

    private void startTimer() {
        new Thread(() -> {
            try {
                for (int i = 15; i >= 0; i--) {
                    final int t = i;
                    sendToAllClients(st -> st.sendRoundTimer(t), null);
                    Thread.sleep(1000);
                    if (currentPhase != Phase.CHOOSING) return;
                }
                autoFillMissingChoices();
                resolveRound();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void autoFillMissingChoices() {
        String[] options = {"r", "p", "s"};
        Random rand = new Random();
        for (Player p : players.values()) {
            if (!p.isEliminated() && p.getCurrentChoice() == null) {
                p.setCurrentChoice(options[rand.nextInt(3)]);
                sendMessage(null, p.getClientName() + " timed out — random choice assigned");
            }
        }
    }

    private void checkAllChosen() {
        boolean allChosen = players.values().stream()
            .filter(p -> !p.isEliminated())
            .allMatch(p -> p.getCurrentChoice() != null);
        if (allChosen) resolveRound();
    }

    private synchronized void resolveRound() {
        if (currentPhase != Phase.CHOOSING) return;
        setPhase(Phase.RESULTS);

        List<Player> activePlayers = getActivePlayers();
        processBattles(activePlayers);
        final List<Player> remaining = getActivePlayers();

        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            if (remaining.size() == 1) {
                endSession(remaining.get(0));
            } else if (remaining.size() == 0) {
                endSession(null);
            } else {
                startRound();
            }
        }).start();
    }

    private List<Player> getActivePlayers() {
        return players.values().stream()
            .filter(p -> !p.isEliminated() && !p.isSpectator())
            .collect(Collectors.toList());
    }

    private void processBattles(List<Player> activePlayers) {
        int size = activePlayers.size();
        for (int i = 0; i < size; i++) {
            Player attacker = activePlayers.get(i);
            Player defender = activePlayers.get((i + 1) % size);
            if (attacker.isEliminated() || defender.isEliminated()) continue;

            String result = determineWinner(attacker.getCurrentChoice(), defender.getCurrentChoice());
            sendMessage(null, String.format("Battle: %s (%s) vs %s (%s) — %s",
                attacker.getClientName(), choiceName(attacker.getCurrentChoice()),
                defender.getClientName(), choiceName(defender.getCurrentChoice()),
                result));

            if ("attacker".equals(result)) {
                attacker.addPoint();
                defender.setEliminated(true);
                syncPoints(attacker);
                sendMessage(null, defender.getClientName() + " was eliminated");
            } else if ("defender".equals(result)) {
                defender.addPoint();
                attacker.setEliminated(true);
                syncPoints(defender);
                sendMessage(null, attacker.getClientName() + " was eliminated");
            }
        }
    }

    private String determineWinner(String a, String b) {
        if (a == null || b == null) return "tie";
        if (a.equals(b)) return "tie";
        Map<String, String> beats = new HashMap<>();
        beats.put("r", "s");
        beats.put("p", "r");
        beats.put("s", "p");
        String aBeats = beats.get(a);
        if (aBeats == null) return "tie";
        return aBeats.equals(b) ? "attacker" : "defender";
    }

    private String choiceName(String code) {
        switch (code) {
            case "r": return "Rock";
            case "p": return "Paper";
            case "s": return "Scissors";
            default: return code;
        }
    }

    private void endSession(Player winner) {
        setPhase(Phase.GAME_OVER);

        if (winner != null) {
            sendMessage(null, "Game Over! " + winner.getClientName() + " wins!");
            Server.INSTANCE.getLeaderboard().recordWin(winner.getClientName());
            Server.INSTANCE.broadcastLeaderboard();
        } else {
            sendMessage(null, "Game Over! It's a tie!");
        }

        List<Player> sorted = players.values().stream()
            .filter(p -> !p.isSpectator())
            .sorted((a, b) -> Integer.compare(b.getPoints(), a.getPoints()))
            .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("=== SCOREBOARD ===\n");
        for (int i = 0; i < sorted.size(); i++) {
            Player p = sorted.get(i);
            sb.append(String.format("%d. %s: %d pts\n", i + 1, p.getClientName(), p.getPoints()));
        }
        sendMessage(null, sb.toString());
    }

    private void resetForNewGame() {
        for (Player p : players.values()) p.reset();
        syncAllPoints();
    }

    private void setPhase(Phase phase) {
        this.currentPhase = phase;
        sendToAllClients(st -> st.sendPhase(phase.name()), null);
    }

    private void syncPoints(Player player) {
        sendToAllClients(st -> st.sendPoints(player.getClientId(), player.getClientName(), player.getPoints()), null);
    }

    private void syncAllPoints() {
        for (Player p : players.values()) syncPoints(p);
    }

    public Phase getCurrentPhase() { return currentPhase; }
}
