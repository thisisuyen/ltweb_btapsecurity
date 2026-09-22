package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iotstar.service.MailService;

@Service
public class MailServiceImpl implements MailService {

  private final JavaMailSender mailSender;
  private final String from;

  public MailServiceImpl(JavaMailSender mailSender,
                         @Value("${spring.mail.username}") String from) {
    this.mailSender = mailSender;
    this.from = from;
  }

  @Override
  public void send(String to, String subject, String content) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(from);
    msg.setTo(to);
    msg.setSubject(subject);
    msg.setText(content);
    mailSender.send(msg);
  }
}