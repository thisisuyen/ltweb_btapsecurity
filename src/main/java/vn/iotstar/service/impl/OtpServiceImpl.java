package vn.iotstar.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.EmailService;
import vn.iotstar.service.OtpService;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.ttl-minutes:5}")
    private int ttlMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.dev-print:true}")
    private boolean devPrint;

    public OtpServiceImpl(
            OtpTokenRepository otpTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.otpTokenRepository = otpTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public String createAndSendOtp(
            User user,
            OtpType type,
            String subject,
            String prefix) {

        if (user == null) {
            throw new RuntimeException("User không tồn tại.");
        }

        if (user.getId() == null) {
            throw new RuntimeException("User chưa được lưu vào database.");
        }

        if (user.getEmail() == null ||
                user.getEmail().isBlank()) {

            throw new RuntimeException(
                    "Email của user đang trống."
            );
        }

        String otp = generateOtp6();

        LocalDateTime now = LocalDateTime.now();

        OtpToken token = new OtpToken();

        token.setUser(user);
        token.setType(type);

        // Không lưu OTP plaintext
        token.setOtpHash(
                passwordEncoder.encode(otp)
        );

        token.setAttempts(0);
        token.setCreatedAt(now);
        token.setExpiresAt(
                now.plusMinutes(ttlMinutes)
        );
        token.setVerifiedAt(null);

        otpTokenRepository.save(token);

        String to = user.getEmail();

        String mailSubject =
                subject == null ? "" : subject;

        String body =
                (prefix == null ? "" : prefix)
                + otp
                + "\n\nMã OTP hết hạn sau "
                + ttlMinutes
                + " phút.";

        /*
         * DEVELOPMENT MODE
         *
         * app.otp.dev-print=true
         *
         * Không gửi mail.
         * OTP xuất hiện trong Console.
         */
        if (devPrint) {

            System.out.println();
            System.out.println(
                    "======================================"
            );
            System.out.println("OTP DEVELOPMENT MODE");
            System.out.println("Email : " + to);
            System.out.println("Type  : " + type);
            System.out.println("OTP   : " + otp);
            System.out.println(
                    "======================================"
            );
            System.out.println();

            return otp;
        }

        /*
         * PRODUCTION MODE
         *
         * Sau khi transaction commit
         * mới gửi email.
         */
        if (TransactionSynchronizationManager
                .isSynchronizationActive()) {

            TransactionSynchronizationManager
                    .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {

                                try {

                                    emailService.send(
                                            to,
                                            mailSubject,
                                            body
                                    );

                                    System.out.println(
                                            "[OTP] Email sent to: "
                                            + to
                                    );

                                } catch (Exception e) {

                                    System.err.println(
                                            "[OTP] Send mail failed: "
                                            + e.getMessage()
                                    );
                                }
                            }
                        }
                    );

        } else {

            emailService.send(
                    to,
                    mailSubject,
                    body
            );
        }

        return otp;
    }

    @Override
    @Transactional
    public void verifyOtpOrThrow(
            User user,
            OtpType type,
            String otp) {

        if (user == null) {
            throw new RuntimeException(
                    "User không tồn tại."
            );
        }

        if (otp == null || otp.isBlank()) {
            throw new RuntimeException(
                    "OTP không được để trống."
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        OtpToken token =
                otpTokenRepository
                    .findFirstByUserIdAndTypeAndVerifiedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                            user.getId(),
                            type,
                            now
                    )
                    .orElseThrow(
                        () -> new RuntimeException(
                            "OTP không tồn tại hoặc đã hết hạn."
                        )
                    );

        if (token.getAttempts() >= maxAttempts) {

            throw new RuntimeException(
                    "Bạn đã nhập sai OTP quá số lần cho phép."
            );
        }

        /*
         * Tăng attempts cho mỗi lần verify.
         */
        token.setAttempts(
                token.getAttempts() + 1
        );

        boolean valid =
                passwordEncoder.matches(
                        otp.trim(),
                        token.getOtpHash()
                );

        if (!valid) {

            otpTokenRepository.save(token);

            throw new RuntimeException(
                    "OTP không đúng."
            );
        }

        token.setVerifiedAt(
                LocalDateTime.now()
        );

        otpTokenRepository.save(token);
    }

    private String generateOtp6() {

        int number =
                secureRandom.nextInt(1_000_000);

        return String.format(
                "%06d",
                number
        );
    }
}