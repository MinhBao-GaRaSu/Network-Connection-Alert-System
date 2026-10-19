/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.doancuoiky;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * MonitoringFrame — Giao diện chính của Hệ thống Giám sát & Cảnh báo Kết nối Mạng.
 * Tác giả: Phùng Nguyễn Huy Hoàng (MSSV: 2380600740)
 */
public class MonitoringFrame extends javax.swing.JFrame {

    // ===== Dữ liệu & Dịch vụ Backend =====
    private final DatabaseManager           dbManager         = DatabaseManager.getInstance();
    private final List<ServerInfo>          serverList        = new ArrayList<>();
    private MonitoringService               monitoringService;
    private ScheduledExecutorService        scheduler;
    private javax.swing.Timer               uiTimer;
    private EmailNotifier                   emailNotifier;
    private LatencyChartPanel               chartPanel;
    private JFrame                          chartFrame;
    private final Map<String, Boolean>      previousStatuses  = new HashMap<>();

    /**
     * Khởi tạo Form MonitoringFrame.
     */
    public MonitoringFrame() {
        initComponents();
        initCustomFeatures();
    }

    /**
     * Khởi tạo các tính năng tùy biến, kết nối CSDL SQLite, LatencyChart và EmailNotifier.
     */
    private void initCustomFeatures() {
        setTitle("Network Connection Alert System — Hệ Thống Giám Sát Kết Nối Mạng");
        setLocationRelativeTo(null);

        // Khởi tạo EmailNotifier và nạp cấu hình từ SQLite
        emailNotifier = new EmailNotifier();
        String from = dbManager.getConfig("email_from", "");
        String pass = dbManager.getConfig("email_pass", "");
        String to   = dbManager.getConfig("email_to", "");
        boolean enabled = "true".equalsIgnoreCase(dbManager.getConfig("email_enabled", "false"));
        if (enabled && !to.isBlank()) {
            emailNotifier.setConfig(from, pass, to);
        }

        // Khởi tạo bảng biểu đồ độ trễ Latency (do Hoàng thiết kế)
        chartPanel = new LatencyChartPanel();

        // Áp dụng StatusCellRenderer (do Hoàng thiết kế) cho cột Status (cột 4)
        tblServers.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());

        // Nạp danh sách máy chủ từ SQLite vào bảng
        loadServersFromDatabase();

        // Khởi tạo MonitoringService kết nối với backend đa luồng
        monitoringService = new MonitoringService(serverList.toArray(new ServerInfo[0]), this::appendLog);
        monitoringService.setEmailNotifier(emailNotifier);

        // Thiết lập Menu Bar tiện ích ở đầu cửa sổ
        setupMenuBar();

        // Trạng thái nút ban đầu
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        appendLog("[HỆ THỐNG] Khởi tạo giao diện hoàn tất. Đã kết nối CSDL SQLite.");
    }

    /**
     * Thiết lập Menu Bar với các tính năng: Biểu đồ Latency, Cấu hình Email, Lịch sử SQLite.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Công cụ
        JMenu menuTools = new JMenu("📊 Tính Năng Mở Rộng");
        menuTools.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JMenuItem itemChart = new JMenuItem("📈 Mở Biểu Đồ Độ Trễ Thời Gian Thực (Latency Chart)");
        itemChart.addActionListener(e -> openLatencyChartWindow());

        JMenuItem itemCheckNow = new JMenuItem("🔄 Quét Ngay Toàn Bộ Máy Chủ");
        itemCheckNow.addActionListener(e -> {
            if (monitoringService != null && !serverList.isEmpty()) {
                monitoringService.checkServers();
                appendLog("[HỆ THỐNG] Đang thực hiện quét thủ công...");
            }
        });

        JMenuItem itemClearLog = new JMenuItem("🗑 Xóa Nhật Ký");
        itemClearLog.addActionListener(e -> txtLog.setText(""));

        menuTools.add(itemChart);
        menuTools.add(itemCheckNow);
        menuTools.addSeparator();
        menuTools.add(itemClearLog);

        // Menu Cài đặt & Lịch sử
        JMenu menuSettings = new JMenu("⚙ Cài Đặt & Lịch Sử");
        menuSettings.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JMenuItem itemEmail = new JMenuItem("📧 Cấu Hình Cảnh Báo Email SMTP (Gmail)");
        itemEmail.addActionListener(e -> openEmailConfigDialog());

        JMenuItem itemHistory = new JMenuItem("📜 Xem Lịch Sử Kiểm Tra (SQLite)");
        itemHistory.addActionListener(e -> openHistoryDialog());

        menuSettings.add(itemEmail);
        menuSettings.add(itemHistory);

        // Menu Thông tin
        JMenu menuAbout = new JMenu("ℹ Thông Tin");
        menuAbout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JMenuItem itemAbout = new JMenuItem("Nhóm Thực Hiện Đồ Án");
        itemAbout.addActionListener(e -> JOptionPane.showMessageDialog(
            this,
            "Đồ Án: Network Connection Alert System\n\n"
            + "1. Trần Bảo Minh (2280601960): Khởi tạo repo, SQLite & DatabaseManager\n"
            + "2. Phạm Tuấn Hùng (2380600780): Core Network đa luồng & Email Alert\n"
            + "3. Phùng Nguyễn Huy Hoàng (2380600740): Thiết kế GUI Swing, Renderer màu & Biểu đồ Latency",
            "Thông Tin Đồ Án",
            JOptionPane.INFORMATION_MESSAGE
        ));
        menuAbout.add(itemAbout);

        menuBar.add(menuTools);
        menuBar.add(menuSettings);
        menuBar.add(menuAbout);

        setJMenuBar(menuBar);
    }

    /**
     * Mở cửa sổ Biểu đồ độ trễ phản hồi thời gian thực LatencyChartPanel.
     */
    public void openLatencyChartWindow() {
        if (chartFrame == null) {
            chartFrame = new JFrame("Biểu Đồ Độ Trễ Thời Gian Thực — Latency Chart (Java2D)");
            chartFrame.setSize(720, 420);
            chartFrame.setLocationRelativeTo(this);
            chartFrame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        }
        chartFrame.setVisible(true);
        chartFrame.toFront();
    }

    /**
     * Mở hộp thoại cấu hình cảnh báo Email SMTP.
     */
    private void openEmailConfigDialog() {
        JDialog dialog = new JDialog(this, "Thiết Lập Email Cảnh Báo Sự Cố", true);
        dialog.setSize(480, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtFrom = new JTextField(dbManager.getConfig("email_from", ""), 20);
        JPasswordField txtPass = new JPasswordField(dbManager.getConfig("email_pass", ""), 20);
        JTextField txtTo = new JTextField(dbManager.getConfig("email_to", ""), 20);
        JCheckBox chkEnable = new JCheckBox("Bật cảnh báo Email khi server mất kết nối / phục hồi");
        chkEnable.setSelected("true".equalsIgnoreCase(dbManager.getConfig("email_enabled", "false")));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(chkEnable, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0; panel.add(new JLabel("Email gửi (Gmail):"), gbc);
        gbc.gridx = 1; panel.add(txtFrom, gbc);

        gbc.gridy = 2; gbc.gridx = 0; panel.add(new JLabel("Mật khẩu ứng dụng (16 ký tự):"), gbc);
        gbc.gridx = 1; panel.add(txtPass, gbc);

        gbc.gridy = 3; gbc.gridx = 0; panel.add(new JLabel("Email nhận cảnh báo:"), gbc);
        gbc.gridx = 1; panel.add(txtTo, gbc);

        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton("Lưu Cấu Hình");
        btnSave.addActionListener(e -> {
            String f = txtFrom.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            String t = txtTo.getText().trim();
            boolean en = chkEnable.isSelected();

            dbManager.setConfig("email_from", f);
            dbManager.setConfig("email_pass", p);
            dbManager.setConfig("email_to", t);
            dbManager.setConfig("email_enabled", String.valueOf(en));

            if (en) emailNotifier.setConfig(f, p, t);
            else emailNotifier.setConfig("", "", "");

            appendLog("[CẤU HÌNH] Đã lưu thiết lập Email cảnh báo.");
            JOptionPane.showMessageDialog(dialog, "Lưu cấu hình thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        JButton btnTest = new JButton("Gửi Email Thử");
        btnTest.addActionListener(e -> {
            String f = txtFrom.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            String t = txtTo.getText().trim();
            if (f.isBlank() || p.isBlank() || t.isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Thread(() -> {
                try {
                    EmailNotifier testNotifier = new EmailNotifier();
                    testNotifier.setConfig(f, p, t);
                    testNotifier.sendEmail("Server Test", "127.0.0.1", "TEST_ALERT (Kiểm tra kết nối)");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "Gửi email thử thành công! Vui lòng kiểm tra hộp thư: " + t, "Thành công", JOptionPane.INFORMATION_MESSAGE));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "Lỗi gửi email: " + ex.getMessage(), "Thất bại", JOptionPane.ERROR_MESSAGE));
                }
            }).start();
        });

        btnPanel.add(btnSave);
        btnPanel.add(btnTest);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dialog.getContentPane().add(panel);
        dialog.setVisible(true);
    }

    /**
     * Mở hộp thoại xem lịch sử kiểm tra từ SQLite bảng status_history.
     */
    private void openHistoryDialog() {
        JDialog dialog = new JDialog(this, "Lịch Sử Giám Sát Kết Nối (SQLite)", true);
        dialog.setSize(680, 420);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"#", "Tên Server", "Host / IP", "Trạng Thái", "Độ Trễ", "Thời Gian"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

        List<DatabaseManager.StatusHistoryRecord> history = dbManager.loadRecentHistory(100);
        for (int i = 0; i < history.size(); i++) {
            DatabaseManager.StatusHistoryRecord r = history.get(i);
            model.addRow(new Object[]{
                i + 1,
                r.getServerName(),
                r.getHost(),
                r.getStatus(),
                r.getResponseTime() > 0 ? (r.getResponseTime() + " ms") : "-",
                r.getCheckedAt()
            });
        }

        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("  Hiển thị 100 bản ghi kiểm tra mới nhất từ SQLite:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton btnClear = new JButton("Xóa Sạch Lịch Sử");
        btnClear.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(dialog, "Xác nhận xóa toàn bộ lịch sử?", "Xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dbManager.clearHistory();
                model.setRowCount(0);
                appendLog("[DB] Đã xóa toàn bộ lịch sử status_history.");
            }
        });
        pnlTop.add(lbl, BorderLayout.WEST);
        pnlTop.add(btnClear, BorderLayout.EAST);

        dialog.getContentPane().setLayout(new BorderLayout(5, 5));
        dialog.getContentPane().add(pnlTop, BorderLayout.NORTH);
        dialog.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // =====================================================
    // GHI LOG
    // =====================================================

    /**
     * Ghi nội dung log lên JTextArea và tự động cuộn xuống cuối.
     */
    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    private String getServerKey(ServerInfo server) {
        return server.getName() + "|" + server.getHost() + "|" + server.getPort();
    }

    /**
     * Kiểm tra và hiển thị popup cảnh báo khi server chuyển trạng thái mất kết nối.
     */
    private void checkForOfflineAlerts() {
        for (ServerInfo server : serverList) {
            if (!server.hasChecked()) continue;

            String serverKey = getServerKey(server);
            boolean currentStatus = server.isStatus();
            Boolean previousStatus = previousStatuses.get(serverKey);

            if (!currentStatus && (previousStatus == null || previousStatus)) {
                String message = "Máy chủ " + server.getName() + " (" + server.getHost() + ") đang MẤT KẾT NỐI!";
                appendLog("[ALERT] " + message);
            }
            previousStatuses.put(serverKey, currentStatus);
        }
    }

    // =====================================================
    // LOAD SERVER TABLE TỪ CSDL SQLITE
    // =====================================================

    /**
     * Nạp danh sách máy chủ từ SQLite vào serverList.
     */
    private void loadServersFromDatabase() {
        serverList.clear();
        List<ServerInfo> list = dbManager.loadServers();
        serverList.addAll(list);
        loadServerTable();
    }

    /**
     * Hiển thị danh sách server lên JTable.
     */
    private void loadServerTable() {
        DefaultTableModel model = (DefaultTableModel) tblServers.getModel();
        model.setRowCount(0);

        for (ServerInfo server : serverList) {
            String status;
            if (server.hasChecked()) {
                status = server.isStatus() ? "ONLINE" : "OFFLINE";
            } else {
                status = "WAITING";
            }

            String responseTime;
            if (server.hasChecked() && server.isStatus()) {
                responseTime = server.getResponseTime() + " ms";
            } else {
                responseTime = "-";
            }

            model.addRow(new Object[]{
                server.getName(),
                server.getHost(),
                server.getPort() == 0 ? "-" : server.getPort(),
                server.getType(),
                status,
                responseTime
            });
        }

        updateStatistics();
    }

    // =====================================================
    // UPDATE STATISTICS
    // =====================================================

    /**
     * Cập nhật thống kê tổng số server, ONLINE và OFFLINE.
     */
    private void updateStatistics() {
        int total = serverList.size();
        int online = 0;
        int offline = 0;

        for (ServerInfo server : serverList) {
            if (server.hasChecked()) {
                if (server.isStatus()) online++;
                else offline++;
            }
        }

        lblStatistics.setText("Total: " + total + " | ONLINE: " + online + " | OFFLINE: " + offline);
    }

    // =====================================================
    // UPDATE SERVER TABLE
    // =====================================================

    /**
     * Cập nhật trạng thái và response time trên JTable và cập nhật LatencyChart.
     */
    private void updateServerTable() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::updateServerTable);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblServers.getModel();
        if (model.getRowCount() != serverList.size()) {
            loadServerTable();
            return;
        }

        for (int i = 0; i < serverList.size(); i++) {
            ServerInfo server = serverList.get(i);
            String status = "WAITING";
            String responseTime = "-";

            if (server.hasChecked()) {
                status = server.isStatus() ? "ONLINE" : "OFFLINE";
                if (server.isStatus()) {
                    responseTime = server.getResponseTime() + " ms";
                }
                // Gửi dữ liệu độ trễ vào Biểu đồ LatencyChartPanel của Hoàng
                chartPanel.addDataPoint(server.getName(), server.isStatus() ? server.getResponseTime() : 0);
            }

            model.setValueAt(status, i, 4);
            model.setValueAt(responseTime, i, 5);
        }

        checkForOfflineAlerts();
        updateStatistics();
    }

    /**
     * Kiểm tra scheduler có đang chạy hay không.
     */
    private boolean isMonitoringRunning() {
        return scheduler != null && !scheduler.isShutdown() && !scheduler.isTerminated();
    }

    // =====================================================
    // START MONITORING
    // =====================================================

    /**
     * Bắt đầu kiểm tra server định kỳ.
     */
    private void startMonitoring() {
        if (isMonitoringRunning()) {
            appendLog("Monitoring is already running.");
            return;
        }

        if (serverList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách máy chủ đang trống! Vui lòng thêm máy chủ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        appendLog("Monitoring started.");

        // Tạo scheduler quét máy chủ định kỳ mỗi 5 giây
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(monitoringService::checkServers, 0, 5, TimeUnit.SECONDS);

        // Cập nhật bảng giao diện mỗi 1 giây
        uiTimer = new javax.swing.Timer(1000, e -> updateServerTable());
        uiTimer.start();

        // Cập nhật trạng thái nút
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
    }

    // =====================================================
    // STOP MONITORING
    // =====================================================

    /**
     * Dừng kiểm tra server.
     */
    private void stopMonitoring() {
        boolean wasRunning = isMonitoringRunning();

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }

        if (uiTimer != null) {
            uiTimer.stop();
            uiTimer = null;
        }

        if (monitoringService != null) {
            monitoringService.stopMonitoring();
        }

        if (wasRunning) {
            appendLog("Monitoring stopped.");
        }

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }

    // =====================================================
    // ADD SERVER
    // =====================================================

    /**
     * Thêm server mới và lưu trực tiếp vào SQLite.
     */
    private void addServer() {
        String name = JOptionPane.showInputDialog(this, "Nhập tên server:");
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim();

        String host = JOptionPane.showInputDialog(this, "Nhập Host hoặc IP (vd: 8.8.8.8 hoặc google.com):");
        if (host == null || host.trim().isEmpty()) return;
        host = host.trim();

        String type = JOptionPane.showInputDialog(this, "Nhập loại kiểm tra (PING hoặc TCP):");
        if (type == null || type.trim().isEmpty()) return;
        type = type.trim().toUpperCase();

        if (!type.equals("PING") && !type.equals("TCP")) {
            JOptionPane.showMessageDialog(this, "Loại kiểm tra chỉ được là PING hoặc TCP!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int port = 0;
        if (type.equals("TCP")) {
            String portText = JOptionPane.showInputDialog(this, "Nhập Cổng TCP (vd: 80, 443):");
            if (portText == null || portText.trim().isEmpty()) return;
            try {
                port = Integer.parseInt(portText.trim());
                if (port < 1 || port > 65535) {
                    JOptionPane.showMessageDialog(this, "Port TCP phải từ 1 đến 65535!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Port phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        ServerInfo newServer = new ServerInfo(name, host, port, type);
        int generatedId = dbManager.saveServer(newServer);
        newServer.setId(generatedId);

        serverList.add(newServer);

        // Cập nhật danh sách cho MonitoringService
        monitoringService.setServers(serverList.toArray(new ServerInfo[0]));

        loadServerTable();
        appendLog("[DB] Added server to SQLite: " + name + " (" + host + ")");
    }

    // =====================================================
    // DELETE SERVER
    // =====================================================

    /**
     * Xóa server đang được chọn khỏi giao diện và SQLite.
     */
    private void deleteServer() {
        int selectedRow = tblServers.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn server cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ServerInfo serverToDelete = serverList.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xóa server '" + serverToDelete.getName() + "' khỏi SQLite không?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        // Xóa khỏi SQLite
        dbManager.deleteServer(serverToDelete.getId());

        // Xóa khỏi danh sách bộ nhớ
        serverList.remove(selectedRow);

        // Cập nhật MonitoringService
        monitoringService.setServers(serverList.toArray(new ServerInfo[0]));

        loadServerTable();
        appendLog("[DB] Deleted server from SQLite: " + serverToDelete.getName());
    }

    @Override
    public void dispose() {
        stopMonitoring();
        dbManager.close();
        if (chartFrame != null) chartFrame.dispose();
        super.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        btnStart = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        lblLog = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblServers = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        lblStatistics = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblTitle.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblTitle.setText("NETWORK MONITORING SYSTEM");

        btnStart.setText("Start Monitoring");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnStop.setText("Stop Monitoring");
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        btnAdd.setText("Add Server");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete Server");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        lblLog.setText("System Log");

        tblServers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Name", "Host", "Port", "Type", "Status", "Response Time"
            }
        ));
        jScrollPane1.setViewportView(tblServers);

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setLineWrap(true);
        txtLog.setRows(5);
        txtLog.setWrapStyleWord(true);
        jScrollPane2.setViewportView(txtLog);

        lblStatistics.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblStatistics.setText("Total: 0 | ONLINE: 0 | OFFLINE: 0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 585, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblLog, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(69, 69, 69)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(127, 127, 127)
                                .addComponent(lblTitle))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btnStart)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblStatistics, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnStop)
                                        .addGap(53, 53, 53)
                                        .addComponent(btnAdd)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addComponent(lblTitle)
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnStop)
                    .addComponent(btnStart)
                    .addComponent(btnDelete))
                .addGap(18, 18, 18)
                .addComponent(lblStatistics)
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLog))
                .addGap(29, 29, 29))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        startMonitoring();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        stopMonitoring();
    }//GEN-LAST:event_btnStopActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addServer();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        deleteServer();
    }//GEN-LAST:event_btnDeleteActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MonitoringFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblLog;
    private javax.swing.JLabel lblStatistics;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTable tblServers;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables
}
