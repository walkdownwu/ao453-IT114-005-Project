package rps.common;

import java.util.List;

public class LeaderboardPayload extends Payload {
    private static final long serialVersionUID = 1L;
    private List<LeaderboardEntry> entries;

    public List<LeaderboardEntry> getEntries() { return entries; }
    public void setEntries(List<LeaderboardEntry> entries) { this.entries = entries; }

    public static class LeaderboardEntry implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String playerName;
        private int totalWins;

        public LeaderboardEntry(String playerName, int totalWins) {
            this.playerName = playerName;
            this.totalWins = totalWins;
        }

        public String getPlayerName() { return playerName; }
        public int getTotalWins() { return totalWins; }
    }
}
