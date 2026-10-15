package com.mycompany.doancuoiky;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TcpService — Kiem tra ket noi TCP Socket qua Port.
 * Tac gia: Pham Tuan Hung (MSSV: 2380600780)
 */
public class TcpService {

    private final int timeoutMs;

    public TcpService() {
        this(2000);
    }

    public TcpService(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean checkTcp(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
