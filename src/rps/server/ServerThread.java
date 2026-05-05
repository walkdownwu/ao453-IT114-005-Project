package rps.server;

import java.io.*;
import java.net.Socket;
import rps.common.*;

public class ServerThread extends Thread {
    private Socket client;
    private String clientName;
    private long clientId;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean isRunning = false;
    private Room currentRoom;

    public ServerThread(Socket socket) {
        this.client = socket;
    }

    public void setClientName(String name) { this.clientName = name; }
    public String getClientName() { return clientName; }
    public long getClientId() { return clientId; }
    public Room getCurrentRoom() { return currentRoom; }

    public void setCurrentRoom(Room room) {
        if (currentRoom != null) currentRoom.removeClient(this);
        currentRoom = room;
    }

    protected void info(String message) {
        System.out.println(String.format("Thread[%d]: %s", clientId, message));
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());
            clientId = Thread.currentThread().threadId();
            isRunning = true;

            Payload fromClient;
            while (isRunning && (fromClient = (Payload) in.readObject()) != null) {
                processPayload(fromClient);
            }
        } catch (Exception e) {
            if (isRunning) info("Client disconnected: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void processPayload(Payload p) {
        if (p == null || p.getPayloadType() == null) return;
        try {
            switch (p.getPayloadType()) {
                case CONNECT:
                    String name = p.getClientName();
                    if (name == null || name.trim().isEmpty()) name = "Player" + clientId;
                    clientName = name.trim();
                    sendClientId(clientId);
                    Room lobby = Server.INSTANCE.getRoom(Constants.LOBBY);
                    if (lobby != null) lobby.addClient(this);
                    Server.INSTANCE.broadcastLeaderboard();
                    break;
                case MESSAGE:
                    if (currentRoom != null && p.getMessage() != null)
                        currentRoom.handleMessage(this, p.getMessage());
                    break;
                case READY:
                    if (currentRoom instanceof GameRoom)
                        ((GameRoom) currentRoom).handleReady(this, Boolean.parseBoolean(p.getMessage()));
                    break;
                case PICK:
                    String choice = p.getMessage();
                    if (choice != null && (choice.equals("r") || choice.equals("p") || choice.equals("s"))) {
                        if (currentRoom instanceof GameRoom)
                            ((GameRoom) currentRoom).handlePick(this, choice);
                    }
                    break;
                case AWAY:
                    if (currentRoom instanceof GameRoom)
                        ((GameRoom) currentRoom).handleAway(this, Boolean.parseBoolean(p.getMessage()));
                    break;
                case RETURN_TO_LOBBY:
                    if (currentRoom instanceof GameRoom)
                        ((GameRoom) currentRoom).handleReturnToLobby(this);
                    break;
                case CREATE_ROOM:
                    if (p instanceof RoomPayload) {
                        RoomPayload rp = (RoomPayload) p;
                        String roomName = p.getMessage();
                        if (roomName != null && !roomName.trim().isEmpty())
                            Server.INSTANCE.handleCreateRoom(this, roomName.trim(), rp.isPrivate());
                    }
                    break;
                case JOIN_ROOM:
                    if (p.getMessage() != null && !p.getMessage().trim().isEmpty())
                        Server.INSTANCE.handleJoinRoom(this, p.getMessage().trim());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            info("Error processing payload " + p.getPayloadType() + ": " + e.getMessage());
        }
    }

    private void cleanup() {
        isRunning = false;
        if (currentRoom != null) currentRoom.removeClient(this);
        try { if (client != null) client.close(); } catch (IOException e) {}
    }

    private boolean send(Payload p) {
        try {
            out.writeObject(p);
            out.flush();
            out.reset();
            return true;
        } catch (IOException e) {
            info("Send failed: " + e.getMessage());
            cleanup();
            return false;
        }
    }

    public boolean sendClientId(long id) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CLIENT_ID);
        p.setClientId(id);
        return send(p);
    }

    public boolean sendConnectionStatus(long id, String name, boolean isConnect) {
        Payload p = new Payload();
        p.setPayloadType(isConnect ? PayloadType.CONNECT : PayloadType.DISCONNECT);
        p.setClientId(id);
        p.setClientName(name);
        return send(p);
    }

    public boolean sendMessage(long senderId, String message) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setClientId(senderId);
        p.setMessage(message);
        return send(p);
    }

    public boolean sendClientSync(long id, String name) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.SYNC_CLIENT);
        p.setClientId(id);
        p.setClientName(name);
        return send(p);
    }

    public boolean sendResetUserList() {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESET_USER_LIST);
        return send(p);
    }

    public boolean sendPoints(long id, String name, int points) {
        PointsPayload p = new PointsPayload();
        p.setPayloadType(PayloadType.POINTS);
        p.setClientId(id);
        p.setClientName(name);
        p.setPoints(points);
        return send(p);
    }

    public boolean sendPhase(String phase) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.PHASE);
        p.setMessage(phase);
        return send(p);
    }

    public boolean sendRoundTimer(int seconds) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.ROUND_TIMER);
        p.setMessage(String.valueOf(seconds));
        return send(p);
    }

    public boolean sendRoomList(java.util.List<RoomPayload.RoomInfo> rooms) {
        RoomPayload p = new RoomPayload();
        p.setPayloadType(PayloadType.ROOM_LIST);
        p.setRooms(rooms);
        return send(p);
    }

    public boolean sendLeaderboard(java.util.List<rps.common.LeaderboardPayload.LeaderboardEntry> entries) {
        LeaderboardPayload p = new LeaderboardPayload();
        p.setPayloadType(PayloadType.LEADERBOARD);
        p.setEntries(entries);
        return send(p);
    }
}
