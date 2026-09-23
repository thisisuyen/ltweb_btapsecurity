package vn.iotstar.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    /*
     * Chỉ dùng DEV.
     *
     * Sau này nên bỏ và chỉ xem OTP ở console.
     */
    private String lastOtpFallback;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public synchronized String consumeLastOtpFallback() {

        String otp = lastOtpFallback;

        lastOtpFallback = null;

        return otp;
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {

        if (dto == null) {
            throw new RuntimeException(
                    "Dữ liệu đăng ký không hợp lệ."
            );
        }

        /*
         * Password confirmation
         */
        if (dto.getPassword() == null ||
                dto.getConfirmPassword() == null ||
                !dto.getPassword().equals(
                        dto.getConfirmPassword())) {

            throw new RuntimeException(
                    "Mật khẩu xác nhận không khớp."
            );
        }

        String username =
                dto.getUsername().trim();

        String email =
                dto.getEmail()
                   .trim()
                   .toLowerCase();

        String fullName =
                dto.getFullName().trim();

        /*
         * Check username
         */
        if (userRepository
                .existsByUsername(username)) {

            throw new RuntimeException(
                    "Username đã tồn tại."
            );
        }

        /*
         * Check email
         */
        if (userRepository
                .existsByEmail(email)) {

            throw new RuntimeException(
                    "Email đã tồn tại."
            );
        }

        /*
         * ROLE_USER phải tồn tại.
         *
         * DataInitializer của bạn sẽ tạo role này.
         */
        Role roleUser =
                roleRepository
                    .findByName("ROLE_USER")
                    .orElseThrow(
                        () -> new RuntimeException(
                            "ROLE_USER không tồn tại trong database."
                        )
                    );

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);

        user.setPassword(
                passwordEncoder.encode(
                        dto.getPassword()
                )
        );

        /*
         * Chưa verify OTP.
         */
        user.setEnabled(false);

        user.getRoles().add(roleUser);

        /*
         * saveAndFlush để đảm bảo User có ID
         * trước khi tạo OTP.
         */
        userRepository.saveAndFlush(user);

        /*
         * Tạo OTP.
         */
        lastOtpFallback =
                otpService.createAndSendOtp(
                        user,
                        OtpType.REGISTER,
                        "OTP Register",
                        "Mã OTP đăng ký: "
                );
    }

    @Override
    @Transactional
    public void verifyRegisterOtp(
            String email,
            String otp) {

        if (email == null ||
                email.isBlank()) {

            throw new RuntimeException(
                    "Email không hợp lệ."
            );
        }

        User user =
                userRepository
                    .findByEmail(
                        email.trim().toLowerCase()
                    )
                    .orElseThrow(
                        () -> new RuntimeException(
                            "Không tìm thấy tài khoản."
                        )
                    );

        /*
         * Nếu đã verify rồi.
         */
        if (user.isEnabled()) {
            throw new RuntimeException(
                    "Tài khoản đã được xác thực."
            );
        }

        otpService.verifyOtpOrThrow(
                user,
                OtpType.REGISTER,
                otp
        );

        /*
         * OTP chính xác.
         */
        user.setEnabled(true);

        userRepository.save(user);
    }

    @Override
    public void resendRegisterOtp(String email) {

        if (email == null ||
                email.isBlank()) {

            throw new RuntimeException(
                    "Email không hợp lệ."
            );
        }

        User user =
                userRepository
                    .findByEmail(
                        email.trim().toLowerCase()
                    )
                    .orElseThrow(
                        () -> new RuntimeException(
                            "Không tìm thấy tài khoản."
                        )
                    );

        if (user.isEnabled()) {
            throw new RuntimeException(
                    "Tài khoản đã được xác thực."
            );
        }

        lastOtpFallback =
                otpService.createAndSendOtp(
                        user,
                        OtpType.REGISTER,
                        "OTP Register",
                        "Mã OTP đăng ký: "
                );
    }

    @Override
    public void forgotPassword(
            ForgotPasswordDTO dto) {

        String email =
                dto.getEmail()
                   .trim()
                   .toLowerCase();

        User user =
                userRepository
                    .findByEmail(email)
                    .orElseThrow(
                        () -> new RuntimeException(
                            "Email không tồn tại."
                        )
                    );

        lastOtpFallback =
                otpService.createAndSendOtp(
                        user,
                        OtpType.FORGOT_PASSWORD,
                        "OTP Reset Password",
                        "Mã OTP reset mật khẩu: "
                );
    }

    @Override
    @Transactional
    public void resetPassword(
            ResetPasswordDTO dto) {

        String email =
                dto.getEmail()
                   .trim()
                   .toLowerCase();

        User user =
                userRepository
                    .findByEmail(email)
                    .orElseThrow(
                        () -> new RuntimeException(
                            "Email không tồn tại."
                        )
                    );

        /*
         * Verify OTP trước.
         */
        otpService.verifyOtpOrThrow(
                user,
                OtpType.FORGOT_PASSWORD,
                dto.getOtp()
        );

        /*
         * Encode password mới.
         */
        user.setPassword(
                passwordEncoder.encode(
                        dto.getNewPassword()
                )
        );

        userRepository.save(user);
    }
}