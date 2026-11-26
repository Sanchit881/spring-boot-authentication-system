package com.authapp.athentication.security;


import com.authapp.athentication.entites.Role;
import com.authapp.athentication.entites.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiry-access-token}")
    private long accessExpiry;

    @Value("${jwt.expire-refrash-token}")
    private long refreshExpiry;

    public JwtService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }


    // Generate Access Token
    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        List<String> roles = user.getRoles() == null ? List.of() : user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessExpiry))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("typ", "access")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }


    // Generate Refresh Token
    public String generateRefreshToken(User user, String jti) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshExpiry))
                .claim("typ", "refresh")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    // Parse and validate JWT
    public Jwt parse(String token) {
        return jwtDecoder.decode(token);
    }

    // Check Access Token
    public boolean isAccessToken(String token) {
        Jwt jwt = parse(token);
        return "access".equals(jwt.getClaimAsString("typ"));
    }

    // Check Refresh Token
    public boolean isRefreshToken(String token) {
        Jwt jwt = parse(token);
        return "refresh".equals(jwt.getClaimAsString("typ"));
    }

    public String getJti(String token) {
        return parse(token).getId();
    }


}
