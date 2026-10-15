/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.doancuoiky;

/**
 * ServerInfo — Lớp thực thể lưu trữ thông tin máy chủ.
 * Tác giả: Trần Bảo Minh (MSSV: 2280601960)
 */
public class ServerInfo {
    private int     id;
    private String  name;
    private String  host;
    private int     port;
    private String  type;
    private boolean status;
    private boolean previousStatus;
    private boolean hasChecked;
    private long    responseTime;

    public ServerInfo(String name, String host, int port, String type) {
        this(0, name, host, port, type);
    }

    public ServerInfo(int id, String name, String host, int port, String type) {
        this.id             = id;
        this.name           = name;
        this.host           = host;
        this.port           = port;
        this.type           = type;
        this.status         = false;
        this.previousStatus = false;
        this.hasChecked     = false;
        this.responseTime   = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isStatus() { return status; }
    public boolean getStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public boolean getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(boolean previousStatus) { this.previousStatus = previousStatus; }
    public boolean hasChecked() { return hasChecked; }
    public void setHasChecked(boolean hasChecked) { this.hasChecked = hasChecked; }
    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
}
