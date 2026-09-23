package vn.iotstar.service;

import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;

public interface AuthService {
    void register(RegisterDTO dto);
    void verifyRegisterOtp(String email, String otp);
    void resendRegisterOtp(String email);

    void forgotPassword(ForgotPasswordDTO dto);
    void resetPassword(ResetPasswordDTO dto);
}