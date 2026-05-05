package rps.server;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import rps.common.*;

public class Server {
    public static Server INSTANCE;

    private int port = Constants.DEFAULT_PORT;
    private Map<String, Room> rooms = new LinkedHashMap<>();
    private Leaderboard leaderboard = new Leaderboard();

    private void start(int port) {
        this.port = port;
        System.out.println("RPS Server starting on port " + port);

        rooms.put(Constants.LOBBY, new Room(Constants.LOBBY, false));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server ready.");
            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    ServerThread thread = new ServerThread(client);
                    thread.start();
                } catch (IOException e) {
                    System.err.println("Failed to accept client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public synchronized Room getRoom(String name) {
        return rooms.get(name);
    }

    public synchronized void handleCreateRoom(ServerThread client, String roomName, boolean isPrivate) {
        if (rooms.containsKey(roomName)) {
            client.sendMessage(-1, "Room '" + roomName + "' already exists.");
            return;
        }
        GameRoom room = new GameRoom(roomName, isPrivate);
        rooms.put(roomName, room);
        room.addClient(client);
        broadcastRoomList();
    }

    public synchronized void handleJoinRoom(ServerThread client, String roomName) {
        Room room = rooms.get(roomName);
        if (room == null) {
            client.sendMessage(-1, "Room '" + roomName + "' not found.");
            return;
        }
        room.addClient(client);
    }

    public synchronized void broadcastRoomList() {
        List<RoomPayload.RoomInfo> roomInfos = rooms.values().stream()
            .filter(r -> !r.getName().equals(Constants.LOBBY))
            .map(r -> {
                Phase phase = (r instanceof GameRoom) ? ((GameRoom) r).getCurrentPhase() : Phase.WAITING;
                return new RoomPayload.RoomInfo(r.getName(), r.getPlayerCount(), r.isPrivate(), phase);
            })
            .collect(Collectors.toList());

        Room lobby = rooms.get(Constants.LOBBY);
        if (lobby != null) {
            lobby.sendToAllClients(st -> st.sendRoomList(roomInfos), null);
        }
    }

    public void broadcastLeaderboard() {
        List<LeaderboardPayload.LeaderboardEntry> entries = leaderboard.getEntries();
        for (Room room : rooms.values()) {
            room.sendToAllClients(st -> st.sendLeaderboard(entries), null);
        }
    }

    public Leaderboard getLeaderboard() { return leaderboard; }

    public static void main(String[] args) {
        INSTANCE = new Server();
        int port = Constants.DEFAULT_PORT;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException e) {}
        }
        INSTANCE.start(port);
    }
}
