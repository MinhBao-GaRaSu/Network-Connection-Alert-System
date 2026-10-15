package com.mycompany.doancuoiky;

import java.util.function.Consumer;

/**
 * MonitoringTask — Kiem tra ket noi va ghi lich su vao SQLite cua nhom truong.
 * Tac gia: Pham Tuan Hung (MSSV: 2380600780)
 */
public class MonitoringTask implements Runnable {

    private final ServerInfo       server;
    private final PingService      pingService;
    private final TcpService       tcpService;
    private final Consumer<String> logConsumer;
    private final EmailNotifier    emailNotifier;

    public MonitoringTask(ServerInfo server, Consumer<String> logConsumer, EmailNotifier emailNotifier) {
        this.server        = server;
        this.logConsumer   = logConsumer;
        this.emailNotifier = emailNotifier;
        this.pingService   = new PingService();
        this.tcpService    = new TcpService();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        boolean result;

        // Kiem tra TCP hay PING
        if ("TCP".equalsIgnoreCase(server.getType())) {
            result = tcpService.checkTcp(server.getHost(), server.getPort());
        } else {
            result = pingService.ping(server.getHost());
        }

        long latency = System.currentTimeMillis() - startTime;
        boolean oldStatus = server.getPreviousStatus();

        server.setStatus(result);
        server.setResponseTime(latency);

        String statusStr = result ? "ONLINE" : "OFFLINE";
        String logMsg = "[" + Thread.currentThread().getName() + "] " 
                      + server.getName() + " (" + server.getHost() + ") -> " 
                      + statusStr + (result ? " (" + latency + "ms)" : "");

        if (logConsumer != null) logConsumer.accept(logMsg);

        // Goi DatabaseManager cua nhom truong de luu lich su vao SQLite
        DatabaseManager.getInstance().insertStatusHistory(
            server.getName(), server.getHost(), statusStr, result ? latency : 0
        );

        // Kiem tra phat hien su co rot mang de ban canh bao Email
        boolean isFirstCheck = !server.hasChecked();
        if ((isFirstCheck && !result) || (!isFirstCheck && oldStatus && !result)) {
            if (emailNotifier != null) emailNotifier.sendAlertAsync(server.getName(), server.getHost(), "OFFLINE");
        } else if (!isFirstCheck && !oldStatus && result) {
            if (emailNotifier != null) emailNotifier.sendAlertAsync(server.getName(), server.getHost(), "ONLINE");
        }

        server.setPreviousStatus(result);
        server.setHasChecked(true);
    }
}
