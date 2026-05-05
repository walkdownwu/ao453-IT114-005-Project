package rps.client.views;

import javax.swing.*;
import java.awt.*;
import rps.client.ClientUI;

public class ReadyPanel extends JPanel {
    private ClientUI parent;
    private JButton readyBtn;
    private JTextArea chatArea;
    private boolean isReady = false;

    public ReadyPanel(ClientUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 46));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Waiting Room", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(203, 166, 247));
        add(title, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(49, 50, 68));
        chatArea.setForeground(new Color(205, 214, 244));
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(88, 91, 112)));
        add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        south.setBackground(new Color(30, 30, 46));

        readyBtn = makeBtn("Ready Up", new Color(166, 227, 161));
        readyBtn.addActionListener(e -> toggleReady());

        JButton leaveBtn = makeBtn("Leave Room", new Color(243, 139, 168));
        leaveBtn.addActionListener(e -> parent.getClient().sendJoinRoom("Lobby"));

        south.add(readyBtn);
        south.add(leaveBtn);
        add(south, BorderLayout.SOUTH);
    }

    private void toggleReady() {
        isReady = !isReady;
        parent.getClient().sendReady(isReady);
        readyBtn.setText(isReady ? "Cancel Ready" : "Ready Up");
        readyBtn.setBackground(isReady ? new Color(243, 139, 168) : new Color(166, 227, 161));
    }

    public void addMessage(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void reset() {
        isReady = false;
        readyBtn.setText("Ready Up");
        readyBtn.setBackground(new Color(166, 227, 161));
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(new Color(30, 30, 46));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
