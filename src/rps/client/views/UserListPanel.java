package rps.client.views;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

public class UserListPanel extends JPanel {
    private DefaultTableModel model;
    private Map<Long, Integer> rowIndex = new LinkedHashMap<>();

    public UserListPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(49, 50, 68));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(88, 91, 112)),
            "Players", 0, 0, new Font("Arial", Font.BOLD, 12), new Color(203, 166, 247)));

        String[] cols = {"Player", "Pts"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setBackground(new Color(49, 50, 68));
        table.setForeground(new Color(205, 214, 244));
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.setGridColor(new Color(69, 71, 90));
        table.getTableHeader().setBackground(new Color(69, 71, 90));
        table.getTableHeader().setForeground(new Color(203, 166, 247));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setShowVerticalLines(false);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void addUser(long clientId, String name, int points) {
        if (rowIndex.containsKey(clientId)) return;
        rowIndex.put(clientId, model.getRowCount());
        model.addRow(new Object[]{name, points});
    }

    public void removeUser(long clientId) {
        Integer row = rowIndex.remove(clientId);
        if (row != null) {
            model.removeRow(row);
            rowIndex.replaceAll((id, r) -> r > row ? r - 1 : r);
        }
    }

    public void updatePoints(long clientId, int points) {
        Integer row = rowIndex.get(clientId);
        if (row != null) model.setValueAt(points, row, 1);
    }

    public void clear() {
        model.setRowCount(0);
        rowIndex.clear();
    }
}
