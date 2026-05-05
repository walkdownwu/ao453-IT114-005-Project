package rps.server;

public class Player {
    private long clientId;
    private String clientName;
    private int points;
    private boolean ready;
    private boolean eliminated;
    private boolean spectator;
    private boolean away;
    private String currentChoice;
    private boolean returnedToLobby;

    public Player(long clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
    }

    public long getClientId() { return clientId; }
    public String getClientName() { return clientName; }

    public int getPoints() { return points; }
    public void addPoint() { points++; }

    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }

    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }

    public boolean isSpectator() { return spectator; }
    public void setSpectator(boolean spectator) { this.spectator = spectator; }

    public boolean isAway() { return away; }
    public void setAway(boolean away) { this.away = away; }

    public String getCurrentChoice() { return currentChoice; }
    public void setCurrentChoice(String choice) { this.currentChoice = choice; }

    public boolean hasReturnedToLobby() { return returnedToLobby; }
    public void setReturnedToLobby(boolean returned) { this.returnedToLobby = returned; }

    public void resetRound() {
        eliminated = false;
        currentChoice = null;
        ready = false;
    }

    public void reset() {
        resetRound();
        points = 0;
        returnedToLobby = false;
    }
}
