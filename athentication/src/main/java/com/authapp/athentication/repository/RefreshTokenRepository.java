package com.authapp.athentication.repository;

import com.authapp.athentication.entites.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJtiAndRevokedFalse(String jti);
}
