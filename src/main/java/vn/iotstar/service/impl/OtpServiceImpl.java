package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.MailService;
import vn.iotstar.service.OtpService;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpServiceImpl implements OtpService {

  private final OtpTokenRepository otpRepo;
  private final MailService mailService;
  private final boolean devPrintOtp;

  private final SecureRandom random = new SecureRandom();

  public OtpServiceImpl(OtpTokenRepository otpRepo,
                        MailService mailService,
                        @Value("${app.otp.dev-print:false}") boolean devPrintOtp) {
    this.otpRepo = otpRepo;
    this.mailService = mailService;
    this.devPrintOtp = devPrintOtp;
  }

  private String genCode6() {
    int n = 100000 + random.nextInt(900000);
    return String.valueOf(n);
  }

  @Override
  @Transactional
  public void sendOtp(String email, OtpType type) {
    String code = genCode6();

    OtpToken token = new OtpToken();
    token.setEmail(email);
    token.setCode(code);
    token.setType(type);
    token.setUsed(false);
    token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
    otpRepo.save(token);

    String subject = (type == OtpType.REGISTER) ? "OTP xác thực đăng ký" : "OTP đặt lại mật khẩu";
    String content =
        "Mã OTP của bạn: " + code + "\n" +
        "Hết hạn sau 5 phút.\n";

    try {
      mailService.send(email, subject, content);
    } catch (MailException ex) {
      if (devPrintOtp) {
        System.out.println("=== DEV OTP (MAIL FAILED) ===");
        System.out.println("Email: " + email);
        System.out.println("Type : " + type);
        System.out.println("OTP  : " + code);
        System.out.println("============================");
      }
      throw new IllegalStateException("Gửi email OTP thất bại (Authentication failed). Kiểm tra SMTP/App Password.");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verify(String email, OtpType type, String code) {
    return otpRepo.findTopByEmailAndTypeAndCodeAndUsedFalseOrderByCreatedAtDesc(email, type, code)
        .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
        .isPresent();
  }
}