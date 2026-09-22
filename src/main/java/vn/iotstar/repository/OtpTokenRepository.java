package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
  Optional<OtpToken> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(String email, OtpType type);
  Optional<OtpToken> findTopByEmailAndTypeAndCodeAndUsedFalseOrderByCreatedAtDesc(String email, OtpType type, String code);
}