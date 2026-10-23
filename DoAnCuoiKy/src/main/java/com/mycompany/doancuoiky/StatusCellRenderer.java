package com.mycompany.doancuoiky;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/*
 * Tác giả: Phùng Nguyễn Huy Hoàng (MSSV: 2380600740)
 */
public class StatusCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String status = (value == null) ? "" : value.toString();

        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD));

        if (isSelected) {
            label.setForeground(table.getSelectionForeground());
            label.setBackground(table.getSelectionBackground());
        } else if ("ONLINE".equalsIgnoreCase(status)) {
            label.setForeground(new Color(0, 120, 0));       // Chữ xanh đậm
            label.setBackground(new Color(215, 250, 215));   // Nền xanh nhạt
        } else if ("OFFLINE".equalsIgnoreCase(status)) {
            label.setForeground(new Color(180, 0, 0));       // Chữ đỏ đậm
            label.setBackground(new Color(255, 220, 220));   // Nền đỏ nhạt
        } else {
            label.setForeground(Color.DARK_GRAY);
            label.setBackground(new Color(245, 245, 245));
        }
        return label;
    }
}