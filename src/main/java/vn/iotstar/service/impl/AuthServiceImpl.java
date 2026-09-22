package vn.iotstar.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepo;
  private final RoleRepository roleRepo;
  private final OtpTokenRepository otpRepo;
  private final OtpService otpService;
  private final PasswordEncoder encoder;

  public AuthServiceImpl(UserRepository userRepo,
                         RoleRepository roleRepo,
                         OtpTokenRepository otpRepo,
                         OtpService otpService,
                         PasswordEncoder encoder) {
    this.userRepo = userRepo;
    this.roleRepo = roleRepo;
    this.otpRepo = otpRepo;
    this.otpService = otpService;
    this.encoder = encoder;
  }

  @Override
  @Transactional
  public String register(RegisterDTO dto) {
    String username = dto.getUsername().trim();
    String email = dto.getEmail().trim().toLowerCase();

    if (!dto.getPassword().equals(dto.getConfirmPassword())) {
      throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
    }
    if (userRepo.existsByUsername(username)) {
      throw new IllegalArgumentException("Username đã tồn tại.");
    }
    if (userRepo.existsByEmail(email)) {
      throw new IllegalArgumentException("Email đã tồn tại.");
    }

    Role userRole = roleRepo.findByName("ROLE_USER")
        .orElseThrow(() -> new IllegalStateException("Thiếu ROLE_USER trong DB."));

    User u = new User();
    u.setUsername(username);
    u.setEmail(email);
    u.setFullName(dto.getFullName());
    u.setImages("/images/avatar-default.png");
    u.setPassword(encoder.encode(dto.getPassword()));
    u.setRole(userRole);
    u.setEnabled(false); // chờ OTP
    userRepo.save(u);

    otpService.sendOtp(email, OtpType.REGISTER);
    return email;
  }

  @Override
  @Transactional
  public boolean verifyRegisterOtp(String email, String code) {
    String e = email.trim().toLowerCase();

    OtpToken token = otpRepo.findTopByEmailAndTypeAndCodeAndUsedFalseOrderByCreatedAtDesc(e, OtpType.REGISTER, code)
        .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
        .orElse(null);

    if (token == null) return false;

    User user = userRepo.findByEmail(e).orElseThrow();
    user.setEnabled(true);
    userRepo.save(user);

    token.setUsed(true);
    otpRepo.save(token);

    return true;
  }

  @Override
  @Transactional
  public void resendRegisterOtp(String email) {
    String e = email.trim().toLowerCase();
    userRepo.findByEmail(e).orElseThrow(() -> new IllegalArgumentException("Email không tồn tại."));
    otpService.sendOtp(e, OtpType.REGISTER);
  }

  @Override
  @Transactional
  public void forgotPassword(ForgotPasswordDTO dto) {
    String e = dto.getEmail().trim().toLowerCase();
    userRepo.findByEmail(e).orElseThrow(() -> new IllegalArgumentException("Email không tồn tại."));
    otpService.sendOtp(e, OtpType.FORGOT_PASSWORD);
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordDTO dto) {
    String e = dto.getEmail().trim().toLowerCase();

    if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
      throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
    }

    OtpToken token = otpRepo.findTopByEmailAndTypeAndCodeAndUsedFalseOrderByCreatedAtDesc(e, OtpType.FORGOT_PASSWORD, dto.getCode())
        .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
        .orElseThrow(() -> new IllegalArgumentException("OTP không đúng hoặc đã hết hạn."));

    User user = userRepo.findByEmail(e).orElseThrow();
    user.setPassword(encoder.encode(dto.getNewPassword()));
    userRepo.save(user);

    token.setUsed(true);
    otpRepo.save(token);
  }
}