package rps.client.views;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import rps.client.ClientUI;
import rps.common.Phase;

public class GamePanel extends JPanel {
    private ClientUI parent;
    private UserListPanel userListPanel;
    private JTextArea eventsArea;
    private JLabel timerLabel;
    private JLabel phaseLabel;
    private Map<String, JButton> choiceButtons = new LinkedHashMap<>();
    private JButton awayBtn;
    private JButton returnBtn;
    private boolean isAway = false;
    private Phase currentPhase = Phase.WAITING;

    private static final Map<String, String> CHOICE_ICONS = new LinkedHashMap<>();
    static {
        CHOICE_ICONS.put("r", "✊  Rock");
        CHOICE_ICONS.put("p", "✋  Paper");
        CHOICE_ICONS.put("s", "✌  Scissors");
    }

    public GamePanel(ClientUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 46));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        userListPanel = new UserListPanel();
        userListPanel.setPreferredSize(new Dimension(180, 0));
        add(userListPanel, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(new Color(30, 30, 46));

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(new Color(30, 30, 46));

        phaseLabel = new JLabel("Waiting...", SwingConstants.LEFT);
        phaseLabel.setFont(new Font("Arial", Font.BOLD, 15));
        phaseLabel.setForeground(new Color(203, 166, 247));
        topBar.add(phaseLabel, BorderLayout.WEST);

        timerLabel = new JLabel("", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setForeground(new Color(249, 226, 175));
        topBar.add(timerLabel, BorderLayout.EAST);
        center.add(topBar, BorderLayout.NORTH);

        eventsArea = new JTextArea();
        eventsArea.setEditable(false);
        eventsArea.setBackground(new Color(49, 50, 68));
        eventsArea.setForeground(new Color(205, 214, 244));
        eventsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        eventsArea.setLineWrap(true);
        eventsArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(eventsArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(88, 91, 112)));
        center.add(scroll, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(8, 8));
        southPanel.setBackground(new Color(30, 30, 46));

        JPanel choicesPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        choicesPanel.setBackground(new Color(30, 30, 46));
        choicesPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        for (Map.Entry<String, String> entry : CHOICE_ICONS.entrySet()) {
            String code = entry.getKey();
            String label = entry.getValue();
            JButton btn = makeChoiceButton(label, code);
            choiceButtons.put(code, btn);
            choicesPanel.add(btn);
        }
        southPanel.add(choicesPanel, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        actionRow.setBackground(new Color(30, 30, 46));

        awayBtn = makeBtn("Mark Away", new Color(250, 179, 135));
        awayBtn.addActionListener(e -> toggleAway());
        actionRow.add(awayBtn);

        returnBtn = makeBtn("Return to Lobby", new Color(243, 139, 168));
        returnBtn.addActionListener(e -> parent.getClient().sendReturnToLobby());
        returnBtn.setVisible(false);
        actionRow.add(returnBtn);

        JButton playAgainBtn = new JButton("Play Again");
        playAgainBtn.setFont(new Font("Arial", Font.BOLD, 13));
        playAgainBtn.setBackground(new Color(166, 227, 161));
        playAgainBtn.setForeground(new Color(30, 30, 46));
        playAgainBtn.setFocusPainted(false);
        playAgainBtn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        playAgainBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playAgainBtn.setName("playAgainBtn");
        playAgainBtn.addActionListener(e -> parent.getClient().sendPlayAgain());
        playAgainBtn.setVisible(false);
        actionRow.add(playAgainBtn);

        southPanel.add(actionRow, BorderLayout.SOUTH);
        center.add(southPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private JButton makeChoiceButton(String label, String code) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.BOLD, 22));
        btn.setBackground(new Color(69, 71, 90));
        btn.setForeground(new Color(205, 214, 244));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setEnabled(false);
        btn.addActionListener(e -> makeChoice(code));
        return btn;
    }

    private void makeChoice(String code) {
        parent.getClient().sendPick(code);
        for (Map.Entry<String, JButton> entry : choiceButtons.entrySet()) {
            if (entry.getKey().equals(code)) {
                entry.getValue().setBackground(new Color(166, 227, 161));
                entry.getValue().setForeground(new Color(30, 30, 46));
            } else {
                entry.getValue().setBackground(new Color(49, 50, 68));
                entry.getValue().setEnabled(false);
            }
        }
        addEvent("You chose " + CHOICE_ICONS.getOrDefault(code, code));
    }

    private void toggleAway() {
        isAway = !isAway;
        parent.getClient().sendAway(isAway);
        awayBtn.setText(isAway ? "I'm Back" : "Mark Away");
        awayBtn.setBackground(isAway ? new Color(166, 227, 161) : new Color(250, 179, 135));
    }

    public void setPhase(Phase phase) {
        this.currentPhase = phase;
        boolean canChoose = phase == Phase.CHOOSING;
        for (JButton btn : choiceButtons.values()) {
            btn.setEnabled(canChoose);
            if (canChoose) btn.setBackground(new Color(69, 71, 90));
            btn.setForeground(new Color(205, 214, 244));
        }
        timerLabel.setText(canChoose ? "15" : "");
        phaseLabel.setText(phaseName(phase));
        returnBtn.setVisible(phase == Phase.GAME_OVER);
        awayBtn.setVisible(phase != Phase.GAME_OVER);
        for (java.awt.Component c : returnBtn.getParent().getComponents()) {
            if (c instanceof JButton && "playAgainBtn".equals(c.getName())) {
                c.setVisible(phase == Phase.GAME_OVER);
            }
        }
    }

    private String phaseName(Phase p) {
        switch (p) {
            case CHOOSING: return "⚔️  Make Your Choice!";
            case RESULTS: return "📊  Results";
            case GAME_OVER: return "🏆  Game Over";
            case READY_CHECK: return "⏳  Waiting for players...";
            default: return "Waiting...";
        }
    }

    public void updateTimer(int seconds) {
        timerLabel.setText(seconds > 0 ? seconds + "s" : "");
        timerLabel.setForeground(seconds <= 5 ? new Color(243, 139, 168) : new Color(249, 226, 175));
    }

    public void addEvent(String msg) {
        eventsArea.append(msg + "\n");
        eventsArea.setCaretPosition(eventsArea.getDocument().getLength());
    }

    public void addUser(long clientId, String name, int points) { userListPanel.addUser(clientId, name, points); }
    public void removeUser(long clientId) { userListPanel.removeUser(clientId); }
    public void updateUserPoints(long clientId, int points) { userListPanel.updatePoints(clientId, points); }
    public void clearUserList() { userListPanel.clear(); }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(new Color(30, 30, 46));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
