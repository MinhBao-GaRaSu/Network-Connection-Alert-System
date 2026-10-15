package com.mycompany.doancuoiky;

import java.net.InetAddress;

/**
 * PingService — Kiem tra ket noi mang qua ICMP Ping.
 * Tac gia: Pham Tuan Hung (MSSV: 2380600780)
 */
public class PingService {

    private final int timeoutMs;

    public PingService() {
        this(2000);
    }

    public PingService(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean ping(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(timeoutMs);
        } catch (Exception e) {
            return false;
        }
    }
}
