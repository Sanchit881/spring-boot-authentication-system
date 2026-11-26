package com.authapp.athentication.service.Imp;

import com.authapp.athentication.dtos.TokenResponseDto;
import com.authapp.athentication.entites.RefreshToken;
import com.authapp.athentication.entites.User;
import com.authapp.athentication.exception.InvalidTokenException;
import com.authapp.athentication.repository.RefreshTokenRepository;
import com.authapp.athentication.security.JwtService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    // Generate both tokens and save refresh token details
    public TokenResponseDto generateTokens(User user) {

        String accessToken = jwtService.generateAccessToken(user);

        String jti = UUID.randomUUID().toString();

        String refreshToken =
                jwtService.generateRefreshToken(user, jti);

        Jwt refreshJwt = jwtService.parse(refreshToken);

        RefreshToken token = new RefreshToken();
        token.setJti(jti);
        token.setUser(user);
        token.setExpiryDate(refreshJwt.getExpiresAt());

        refreshTokenRepository.save(token);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    // Validate and rotate refresh token
    @Transactional
    public TokenResponseDto refreshAccessToken(String refreshToken) {

        // 1. Decode and validate JWT signature, issuer and expiry
        Jwt jwt = jwtService.parse(refreshToken);

        // 2. Check token type
        if (!"refresh".equals(jwt.getClaimAsString("typ"))) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // 3. Get unique token ID
        String jti = jwt.getId();

        // 4. Find active token in database
        RefreshToken storedToken =
                refreshTokenRepository.findByJtiAndRevokedFalse(jti)
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        "Refresh token revoked or not found"
                                ));

        // 5. Check database expiry
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            throw new InvalidTokenException("Refresh token expired");
        }

        // 6. Invalidate old refresh token
        storedToken.setRevoked(true);

        // 7. Generate new access token and refresh token
        return generateTokens(storedToken.getUser());
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token not found");
        }

        try {
            // Decode and validate refresh JWT
            Jwt jwt = jwtService.parse(refreshToken);

            // Ensure this is a refresh token
            if (!"refresh".equals(jwt.getClaimAsString("typ"))) {
                throw new InvalidTokenException("Invalid refresh token");
            }

            // Extract token ID
            String jti = jwt.getId();

            // Find active refresh token in database
            refreshTokenRepository.findByJtiAndRevokedFalse(jti)
                    .ifPresent(token -> token.setRevoked(true));

        } catch (Exception e) {
            throw new InvalidTokenException("Refresh token notfound");
        }
    }
}
