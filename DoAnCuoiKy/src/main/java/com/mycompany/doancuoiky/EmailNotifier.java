package com.mycompany.doancuoiky;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EmailNotifier — Gui email canh bao bat dong bo qua SMTP.
 * Tac gia: Pham Tuan Hung (MSSV: 2380600780)
 */
public class EmailNotifier {

    private static final ExecutorService emailExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "EmailSender-Thread");
        t.setDaemon(true);
        return t;
    });

    private boolean emailEnabled = false;
    private String  smtpHost     = "smtp.gmail.com";
    private int     smtpPort     = 587;
    private String  emailFrom    = "";
    private String  appPassword  = "";
    private String  emailTo      = "";

    public EmailNotifier() {}

    public void setConfig(String emailFrom, String appPassword, String emailTo) {
        this.emailFrom = emailFrom;
        this.appPassword = appPassword;
        this.emailTo = emailTo;
        this.emailEnabled = (emailTo != null && !emailTo.isBlank());
    }

    public void sendAlertAsync(String serverName, String host, String status) {
        if (!emailEnabled || emailTo == null || emailTo.isBlank()) return;

        emailExecutor.submit(() -> {
            try {
                sendEmail(serverName, host, status);
                System.out.println("[EMAIL] Da phat canh bao toi: " + emailTo + " ve server: " + serverName);
            } catch (Exception e) {
                System.err.println("[EMAIL] Gui email that bai: " + e.getMessage());
            }
        });
    }

    public void sendEmail(String serverName, String host, String status) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailFrom, appPassword);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(emailFrom, "Network Monitoring Alert"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
        msg.setSubject("[CANH BAO] Server " + serverName + " dang " + status);
        msg.setContent("<h3>Phat hien su co giam sat mang:</h3>"
                     + "<p>Server: <b>" + serverName + "</b></p>"
                     + "<p>Host: " + host + "</p>"
                     + "<p>Trang thai: <b>" + status + "</b></p>", "text/html; charset=utf-8");

        Transport.send(msg);
    }
}
