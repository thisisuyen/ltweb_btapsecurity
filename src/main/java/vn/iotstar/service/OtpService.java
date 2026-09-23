package vn.iotstar.service;

import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.User;

public interface OtpService {

    String createAndSendOtp(
            User user,
            OtpType type,
            String subject,
            String prefix
    );

    void verifyOtpOrThrow(
            User user,
            OtpType type,
            String otp
    );
}