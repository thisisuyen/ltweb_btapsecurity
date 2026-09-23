package vn.iotstar.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;

public interface OtpTokenRepository
        extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken>
    findFirstByUserIdAndTypeAndVerifiedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId,
            OtpType type,
            LocalDateTime now
    );
}