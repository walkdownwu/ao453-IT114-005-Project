package rps.server;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import rps.common.LeaderboardPayload;

public class Leaderboard {
    private static final String FILE_PATH = "leaderboard.dat";
    private Map<String, Integer> wins = new LinkedHashMap<>();

    public Leaderboard() {
        load();
    }

    public synchronized void recordWin(String playerName) {
        wins.put(playerName, wins.getOrDefault(playerName, 0) + 1);
        save();
    }

    public synchronized List<LeaderboardPayload.LeaderboardEntry> getEntries() {
        return wins.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .map(e -> new LeaderboardPayload.LeaderboardEntry(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private void load() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            wins = (Map<String, Integer>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Leaderboard load failed, starting fresh: " + e.getMessage());
            wins = new LinkedHashMap<>();
        }
    }

    private void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(wins);
        } catch (IOException e) {
            System.err.println("Leaderboard save failed: " + e.getMessage());
        }
    }
}
