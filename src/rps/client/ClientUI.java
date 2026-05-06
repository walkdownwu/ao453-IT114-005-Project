package rps.client;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import rps.client.views.*;
import rps.common.*;
public class ClientUI extends JFrame implements IClientEvents {
    private Client client;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private ConnectionPanel connectionPanel;
    private LobbyPanel lobbyPanel;
    private ReadyPanel readyPanel;
    private GamePanel gamePanel;
    public ClientUI() {
        super("Rock Paper Scissors");
        client = new Client();
        client.addCallback(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(700, 500));
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(30, 30, 46));
        connectionPanel = new ConnectionPanel(this);
        lobbyPanel = new LobbyPanel(this);
        readyPanel = new ReadyPanel(this);
        gamePanel = new GamePanel(this);
        cardPanel.add(connectionPanel, "connection");
        cardPanel.add(lobbyPanel, "lobby");
        cardPanel.add(readyPanel, "ready");
        cardPanel.add(gamePanel, "game");
        add(cardPanel);
        cardLayout.show(cardPanel, "connection");
        setVisible(true);
    }
    public void connect(String username, String host, int port) {
        client.setClientName(username);
        if (!client.connectToServer(host, port))
            JOptionPane.showMessageDialog(this, "Failed to connect to server at " + host + ":" + port);
    }
    public Client getClient() { return client; }
    public void showPanel(String name) { SwingUtilities.invokeLater(() -> cardLayout.show(cardPanel, name)); }
    @Override public void onClientId(long id) { SwingUtilities.invokeLater(() -> showPanel("lobby")); }
    @Override public void onClientConnect(long clientId, String clientName) {
        SwingUtilities.invokeLater(() -> {
            gamePanel.addUser(clientId, clientName, 0);
            gamePanel.addEvent(clientName + " joined");
            readyPanel.addMessage(clientName + " joined the room");
        });
    }
    @Override public void onClientDisconnect(long clientId, String clientName) {
        SwingUtilities.invokeLater(() -> {
            gamePanel.removeUser(clientId);
            gamePanel.addEvent(clientName + " disconnected");
            readyPanel.addMessage(clientName + " left the room");
        });
    }
    @Override public void onMessageReceived(long clientId, String message) {
        SwingUtilities.invokeLater(() -> { gamePanel.addEvent(message); readyPanel.addMessage(message); });
    }
    @Override public void onSyncClient(long clientId, String clientName) {
        SwingUtilities.invokeLater(() -> gamePanel.addUser(clientId, clientName, 0));
    }
    @Override public void onResetUserList() { SwingUtilities.invokeLater(() -> gamePanel.clearUserList()); }
    @Override public void onPoints(long clientId, String clientName, int points) {
        SwingUtilities.invokeLater(() -> gamePanel.updateUserPoints(clientId, points));
    }
    @Override public void onPhase(String phase) {
        SwingUtilities.invokeLater(() -> {
            Phase p = Phase.valueOf(phase);
            switch (p) {
                case WAITING:
                    showPanel("lobby");
                    readyPanel.reset();
                    gamePanel.clearUserList();
                    break;
                case READY_CHECK:
                    showPanel("ready");
                    readyPanel.reset();
                    gamePanel.clearUserList();
                    break;
                case CHOOSING:
                    showPanel("game");
                    gamePanel.setPhase(p);
                    break;
                case RESULTS:
                case GAME_OVER:
                    showPanel("game");
                    gamePanel.setPhase(p);
                    break;
                default: break;
            }
        });
    }
    @Override public void onRoundTimer(int seconds) { SwingUtilities.invokeLater(() -> gamePanel.updateTimer(seconds)); }
    @Override public void onRoomList(List<RoomPayload.RoomInfo> rooms) { SwingUtilities.invokeLater(() -> lobbyPanel.updateRoomList(rooms)); }
    @Override public void onLeaderboard(List<LeaderboardPayload.LeaderboardEntry> entries) { SwingUtilities.invokeLater(() -> lobbyPanel.updateLeaderboard(entries)); }
    public static void main(String[] args) { SwingUtilities.invokeLater(ClientUI::new); }
}