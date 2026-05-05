package rps.client.views;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import rps.client.ClientUI;
import rps.common.*;

public class LobbyPanel extends JPanel {
    private ClientUI parent;
    private DefaultTableModel roomTableModel;
    private DefaultTableModel leaderboardModel;
    private JTable roomTable;

    public LobbyPanel(ClientUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 46));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("✊ RPS Lobby", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(203, 166, 247));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildRoomPanel(), buildLeaderboardPanel());
        split.setDividerLocation(520);
        split.setBackground(new Color(30, 30, 46));
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildRoomPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(new Color(49, 50, 68));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(88, 91, 112)),
            "Game Rooms", 0, 0, new Font("Arial", Font.BOLD, 13), new Color(203, 166, 247)));

        String[] cols = {"Room Name", "Players", "Status", "Type"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        roomTable = new JTable(roomTableModel);
        styleTable(roomTable);
        p.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildLeaderboardPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(new Color(49, 50, 68));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(88, 91, 112)),
            "🏆 Leaderboard", 0, 0, new Font("Arial", Font.BOLD, 13), new Color(249, 226, 175)));

        String[] cols = {"#", "Player", "Wins"};
        leaderboardModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lbTable = new JTable(leaderboardModel);
        styleTable(lbTable);
        p.add(new JScrollPane(lbTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        p.setBackground(new Color(30, 30, 46));

        JButton joinPublic = makeBtn("Join Public Room", new Color(137, 180, 250));
        joinPublic.addActionListener(e -> joinSelectedRoom());

        JButton createPublic = makeBtn("Create Public Room", new Color(166, 227, 161));
        createPublic.addActionListener(e -> createRoom(false));

        JButton createPrivate = makeBtn("Create Private Room", new Color(243, 139, 168));
        createPrivate.addActionListener(e -> createRoom(true));

        JButton joinPrivate = makeBtn("Join Private Room", new Color(250, 179, 135));
        joinPrivate.addActionListener(e -> joinByName());

        p.add(joinPublic);
        p.add(createPublic);
        p.add(createPrivate);
        p.add(joinPrivate);
        return p;
    }

    private void joinSelectedRoom() {
        int row = roomTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a room first."); return; }
        String name = (String) roomTableModel.getValueAt(row, 0);
        boolean isPrivate = "Private".equals(roomTableModel.getValueAt(row, 3));
        if (isPrivate) {
            JOptionPane.showMessageDialog(this, "That is a private room. Use 'Join Private Room' and enter the name.");
            return;
        }
        parent.getClient().sendJoinRoom(name);
    }

    private void joinByName() {
        String name = JOptionPane.showInputDialog(this, "Enter room name:");
        if (name != null && !name.trim().isEmpty()) {
            parent.getClient().sendJoinRoom(name.trim());
        }
    }

    private void createRoom(boolean isPrivate) {
        String name = JOptionPane.showInputDialog(this, "Enter room name:");
        if (name == null || name.trim().isEmpty()) return;
        parent.getClient().sendCreateRoom(name.trim(), isPrivate);
    }

    public void updateRoomList(List<RoomPayload.RoomInfo> rooms) {
        SwingUtilities.invokeLater(() -> {
            roomTableModel.setRowCount(0);
            for (RoomPayload.RoomInfo r : rooms) {
                if (!r.isPrivate()) {
                    roomTableModel.addRow(new Object[]{
                        r.getName(), r.getPlayerCount(), r.getPhase().name(), "Public"
                    });
                }
            }
        });
    }

    public void updateLeaderboard(List<LeaderboardPayload.LeaderboardEntry> entries) {
        SwingUtilities.invokeLater(() -> {
            leaderboardModel.setRowCount(0);
            for (int i = 0; i < entries.size(); i++) {
                LeaderboardPayload.LeaderboardEntry e = entries.get(i);
                leaderboardModel.addRow(new Object[]{i + 1, e.getPlayerName(), e.getTotalWins()});
            }
        });
    }

    private void styleTable(JTable table) {
        table.setBackground(new Color(49, 50, 68));
        table.setForeground(new Color(205, 214, 244));
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.setGridColor(new Color(88, 91, 112));
        table.getTableHeader().setBackground(new Color(69, 71, 90));
        table.getTableHeader().setForeground(new Color(203, 166, 247));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setSelectionBackground(new Color(88, 91, 112));
        table.setSelectionForeground(Color.WHITE);
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(new Color(30, 30, 46));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
