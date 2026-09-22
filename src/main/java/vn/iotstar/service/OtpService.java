package vn.iotstar.service;

import vn.iotstar.entity.OtpType;

public interface OtpService {
  void sendOtp(String email, OtpType type);
  boolean verify(String email, OtpType type, String code);
}