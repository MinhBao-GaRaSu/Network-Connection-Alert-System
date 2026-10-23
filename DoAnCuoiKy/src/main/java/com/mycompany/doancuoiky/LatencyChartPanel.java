package com.mycompany.doancuoiky;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * LatencyChartPanel — Vẽ biểu đồ độ trễ phản hồi thời gian thực bằng Java2D.
 * Tác giả: Phùng Nguyễn Huy Hoàng (MSSV: 2380600740)
 */
public class LatencyChartPanel extends JPanel {

    private static final int MAX_POINTS = 25;
    private static final Color[] COLORS = {
        new Color(41, 182, 246), new Color(255, 112, 67),
        new Color(102, 187, 106), new Color(171, 71, 188)
    };

    private final Map<String, List<Long>> latencyData = new LinkedHashMap<>();
    private final Map<String, Color>      colorMap    = new LinkedHashMap<>();

    public LatencyChartPanel() {
        setBackground(new Color(25, 25, 35)); // Nền tối hiện đại
        setPreferredSize(new Dimension(650, 320));
    }

    public synchronized void addDataPoint(String serverName, long responseTime) {
        latencyData.computeIfAbsent(serverName, k -> new ArrayList<>());
        List<Long> points = latencyData.get(serverName);
        points.add(responseTime);
        if (points.size() > MAX_POINTS) points.remove(0);

        if (!colorMap.containsKey(serverName)) {
            colorMap.put(serverName, COLORS[colorMap.size() % COLORS.length]);
        }
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), pad = 50;

        // Vẽ 2 trục tọa độ X, Y
        g2.setColor(Color.GRAY);
        g2.drawLine(pad, h - pad, w - pad, h - pad);
        g2.drawLine(pad, pad, pad, h - pad);

        // Tìm độ trễ lớn nhất để chia tỷ lệ đồ thị
        long maxLat = 100;
        for (List<Long> list : latencyData.values()) {
            for (Long val : list) if (val > maxLat) maxLat = val;
        }

        // Nhãn trục Y
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString(maxLat + " ms", 10, pad + 10);
        g2.drawString("0 ms", 15, h - pad);

        // Vẽ chú thích tên server và các đường sóng biểu đồ
        int legendX = pad + 10;
        for (Map.Entry<String, List<Long>> entry : latencyData.entrySet()) {
            String name = entry.getKey();
            List<Long> pts = entry.getValue();
            Color c = colorMap.get(name);
            g2.setColor(c);

            g2.fillRect(legendX, 15, 12, 12);
            g2.drawString(name, legendX + 16, 25);
            legendX += 140;

            if (pts.size() < 2) continue;
            double stepX = (double) (w - 2 * pad) / (MAX_POINTS - 1);
            for (int i = 0; i < pts.size() - 1; i++) {
                int x1 = (int) (pad + i * stepX);
                int y1 = (int) ((h - pad) - ((double) pts.get(i) / maxLat * (h - 2 * pad)));
                int x2 = (int) (pad + (i + 1) * stepX);
                int y2 = (int) ((h - pad) - ((double) pts.get(i + 1) / maxLat * (h - 2 * pad)));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawLine(x1, y1, x2, y2);
                g2.fillOval(x1 - 3, y1 - 3, 6, 6);
            }
        }
    }
}