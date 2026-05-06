package rps.server;
import java.util.*;
import java.util.function.Function;
public class Room {
    private String name;
    private boolean isPrivate;
    protected List<ServerThread> clients = new ArrayList<>();
    public Room(String name, boolean isPrivate) { this.name = name; this.isPrivate = isPrivate; }
    public String getName() { return name; }
    public boolean isPrivate() { return isPrivate; }
    public int getPlayerCount() { return clients.size(); }
    protected synchronized void addClient(ServerThread client) {
        if (!clients.contains(client)) {
            clients.add(client);
            client.setCurrentRoom(this);
            onClientAdded(client);
        }
    }
    protected synchronized void removeClient(ServerThread client) {
        if (clients.remove(client)) { onClientRemoved(client); }
    }
    protected void onClientAdded(ServerThread client) {
        sendConnectionStatus(client, true);
        syncExistingClients(client);
        sendMessage(null, client.getClientName() + " joined the room");
        if (!(this instanceof GameRoom)) {
            client.sendPhase("WAITING");
            Server.INSTANCE.broadcastRoomList();
        }
    }
    protected void onClientRemoved(ServerThread client) {
        sendConnectionStatus(client, false);
        sendMessage(null, client.getClientName() + " left the room");
    }
    private void syncExistingClients(ServerThread newClient) {
        for (ServerThread existing : clients) {
            if (existing != newClient) newClient.sendClientSync(existing.getClientId(), existing.getClientName());
        }
    }
    protected void sendConnectionStatus(ServerThread client, boolean isConnect) {
        sendToAllClients(st -> st.sendConnectionStatus(client.getClientId(), client.getClientName(), isConnect), client);
    }
    protected void sendMessage(ServerThread sender, String message) {
        long senderId = sender == null ? -1 : sender.getClientId();
        sendToAllClients(st -> st.sendMessage(senderId, message), null);
    }
    public void sendToAllClients(Function<ServerThread, Boolean> action, ServerThread exclude) {
        List<ServerThread> snapshot;
        synchronized (this) { snapshot = new ArrayList<>(clients); }
        for (ServerThread st : snapshot) { if (st != exclude) action.apply(st); }
    }
    public void handleMessage(ServerThread sender, String message) { sendMessage(sender, message); }
    public void handleDisconnect(ServerThread client) { removeClient(client); }
}