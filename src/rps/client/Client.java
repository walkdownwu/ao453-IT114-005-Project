package rps.client;

import java.io.*;
import java.net.*;
import rps.common.*;

public class Client {
    private Socket server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isRunning = false;
    private String clientName = "";
    private long clientId = -1;
    private IClientEvents events;

    public void setClientName(String name) { this.clientName = name; }
    public String getClientName() { return clientName; }
    public long getClientId() { return clientId; }
    public void addCallback(IClientEvents events) { this.events = events; }

    public boolean connectToServer(String host, int port) {
        try {
            server = new Socket(host, port);
            out = new ObjectOutputStream(server.getOutputStream());
            in = new ObjectInputStream(server.getInputStream());
            isRunning = true;
            startListening();
            sendConnect();
            return true;
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    private void startListening() {
        Thread t = new Thread(() -> {
            try {
                Payload p;
                while (isRunning && (p = (Payload) in.readObject()) != null) {
                    processPayload(p);
                }
            } catch (Exception e) {
                if (isRunning) System.err.println("Connection lost: " + e.getMessage());
            } finally {
                disconnect();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void processPayload(Payload p) {
        if (events == null) return;
        switch (p.getPayloadType()) {
            case CLIENT_ID:
                clientId = p.getClientId();
                events.onClientId(clientId);
                break;
            case CONNECT:
                events.onClientConnect(p.getClientId(), p.getClientName());
                break;
            case DISCONNECT:
                events.onClientDisconnect(p.getClientId(), p.getClientName());
                break;
            case MESSAGE:
                events.onMessageReceived(p.getClientId(), p.getMessage());
                break;
            case SYNC_CLIENT:
                events.onSyncClient(p.getClientId(), p.getClientName());
                break;
            case RESET_USER_LIST:
                events.onResetUserList();
                break;
            case POINTS:
                PointsPayload pp = (PointsPayload) p;
                events.onPoints(pp.getClientId(), pp.getClientName(), pp.getPoints());
                break;
            case PHASE:
                events.onPhase(p.getMessage());
                break;
            case ROUND_TIMER:
                events.onRoundTimer(Integer.parseInt(p.getMessage()));
                break;
            case ROOM_LIST:
                RoomPayload rp = (RoomPayload) p;
                events.onRoomList(rp.getRooms());
                break;
            case LEADERBOARD:
                LeaderboardPayload lp = (LeaderboardPayload) p;
                events.onLeaderboard(lp.getEntries());
                break;
            default:
                break;
        }
    }

    private boolean send(Payload p) {
        try {
            out.writeObject(p);
            out.flush();
            out.reset();
            return true;
        } catch (IOException e) {
            System.err.println("Send error: " + e.getMessage());
            return false;
        }
    }

    public void sendConnect() {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CONNECT);
        p.setClientName(clientName);
        send(p);
    }

    public void sendReady(boolean ready) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.READY);
        p.setMessage(String.valueOf(ready));
        send(p);
    }

    public void sendPick(String choice) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.PICK);
        p.setMessage(choice);
        send(p);
    }

    public void sendAway(boolean away) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.AWAY);
        p.setMessage(String.valueOf(away));
        send(p);
    }

    public void sendReturnToLobby() {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RETURN_TO_LOBBY);
        send(p);
    }

    public void sendPlayAgain() {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.PLAY_AGAIN);
        send(p);
    }

    public void sendCreateRoom(String name, boolean isPrivate) {
        RoomPayload p = new RoomPayload();
        p.setPayloadType(PayloadType.CREATE_ROOM);
        p.setMessage(name);
        p.setPrivate(isPrivate);
        send(p);
    }

    public void sendJoinRoom(String name) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(name);
        send(p);
    }

    public void disconnect() {
        isRunning = false;
        try { if (server != null) server.close(); } catch (IOException e) {}
    }
}
