package vn.iotstar.service;

import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;

public interface AuthService {
  String register(RegisterDTO dto);
  boolean verifyRegisterOtp(String email, String code);
  void resendRegisterOtp(String email);

  void forgotPassword(ForgotPasswordDTO dto);
  void resetPassword(ResetPasswordDTO dto);
}