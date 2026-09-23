package vn.iotstar.service;

public interface EmailService {
    void send(String to, String subject, String content);
}