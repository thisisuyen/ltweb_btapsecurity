package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import vn.iotstar.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String subject, String content) {
        try {
            if (to == null || to.isBlank()) {
                throw new IllegalArgumentException("Email người nhận (to) đang trống.");
            }
            if (from == null || from.isBlank()) {
                // nếu spring.mail.username chưa set, gửi mail chắc chắn fail
                throw new IllegalArgumentException("spring.mail.username (MAIL_USERNAME) đang trống.");
            }

            SimpleMailMessage msg = new SimpleMailMessage();
            // Gmail: from nên là chính username
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject == null ? "" : subject);
            msg.setText(content == null ? "" : content);

            mailSender.send(msg);
        } catch (MailException ex) {
            // lỗi SMTP (auth fail, wrong password, blocked, timeout...)
            throw new RuntimeException("Gửi email thất bại: " + ex.getMessage(), ex);
        }
    }
}