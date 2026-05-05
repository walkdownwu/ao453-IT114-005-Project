package rps.client.views;

import javax.swing.*;
import java.awt.*;
import rps.client.ClientUI;

public class ConnectionPanel extends JPanel {
    private ClientUI parent;
    private JTextField usernameField;
    private JTextField hostField;
    private JTextField portField;

    public ConnectionPanel(ClientUI parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 46));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(49, 50, 68));
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("✊ Rock Paper Scissors", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(203, 166, 247));
        gbc.gridwidth = 2;
        gbc.gridy = 0;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        card.add(makeLabel("Username"), gbc);
        gbc.gridx = 1;
        usernameField = makeField("Player1");
        card.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        card.add(makeLabel("Host"), gbc);
        gbc.gridx = 1;
        hostField = makeField("localhost");
        card.add(hostField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        card.add(makeLabel("Port"), gbc);
        gbc.gridx = 1;
        portField = makeField("3001");
        card.add(portField, gbc);

        JButton connectBtn = new JButton("Connect");
        connectBtn.setFont(new Font("Arial", Font.BOLD, 14));
        connectBtn.setBackground(new Color(137, 180, 250));
        connectBtn.setForeground(new Color(30, 30, 46));
        connectBtn.setFocusPainted(false);
        connectBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        connectBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectBtn.addActionListener(e -> doConnect());

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 8, 8);
        card.add(connectBtn, gbc);

        add(card);
    }

    private void doConnect() {
        String username = usernameField.getText().trim();
        String host = hostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number.");
            return;
        }
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.");
            return;
        }
        parent.connect(username, host, port);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(205, 214, 244));
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        return l;
    }

    private JTextField makeField(String placeholder) {
        JTextField f = new JTextField(placeholder, 14);
        f.setBackground(new Color(69, 71, 90));
        f.setForeground(new Color(205, 214, 244));
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 91, 112)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        return f;
    }
}
