package com.mycompany.doancuoiky;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * MonitoringService — Quan ly thread pool kiem tra dong thoi tat ca server.
 * Tac gia: Pham Tuan Hung (MSSV: 2380600780)
 */
public class MonitoringService {

    private ServerInfo[]           servers;
    private ExecutorService        executor;
    private final Consumer<String> logConsumer;
    private EmailNotifier          emailNotifier;

    public MonitoringService(ServerInfo[] servers, Consumer<String> logConsumer) {
        this.servers     = servers;
        this.logConsumer = logConsumer;
        this.executor    = createExecutor();
    }

    public void setEmailNotifier(EmailNotifier emailNotifier) { this.emailNotifier = emailNotifier; }
    public void setServers(ServerInfo[] servers) { this.servers = servers; }

    public void checkServers() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = createExecutor();
        }
        if (servers != null) {
            for (ServerInfo server : servers) {
                executor.submit(new MonitoringTask(server, logConsumer, emailNotifier));
            }
        }
    }

    public void stopMonitoring() {
        if (executor != null && !executor.isShutdown()) executor.shutdownNow();
    }

    private ExecutorService createExecutor() {
        int threads = (servers != null && servers.length > 0) ? Math.max(4, servers.length) : 4;
        return Executors.newFixedThreadPool(threads);
    }

    // ===== HÀM MAIN TEST ĐỘC LẬP DÀNH CHO HÙNG (BẤM SHIFT + F6 ĐỂ CHẠY) =====
    public static void main(String[] args) {
        System.out.println("=== TEST CORE NETWORK MONITORING DA LUONG (PHAM TUAN HUNG) ===");
        
        ServerInfo[] testServers = {
            new ServerInfo("Google DNS", "8.8.8.8", 0, "PING"),
            new ServerInfo("Cloudflare DNS", "1.1.1.1", 0, "PING"),
            new ServerInfo("Web Google HTTPS", "google.com", 443, "TCP")
        };

        MonitoringService service = new MonitoringService(testServers, logMsg -> System.out.println("LOG: " + logMsg));
        System.out.println("Dang khoi chay thread pool kiem tra dong thoi...");
        service.checkServers();

        try {
            Thread.sleep(2000); // Cho cac luong hoan thanh kiem tra
        } catch (InterruptedException ignored) {}
        service.stopMonitoring();
        System.out.println("=== KIEM TRA HOAN TAT THANH CONG! ===");
    }
}
