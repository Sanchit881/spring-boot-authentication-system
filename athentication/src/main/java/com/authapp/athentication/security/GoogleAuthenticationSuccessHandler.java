package com.authapp.athentication.security;


import com.authapp.athentication.dtos.AccessTokenResponseDto;
import com.authapp.athentication.dtos.TokenResponseDto;
import com.authapp.athentication.entites.User;
import com.authapp.athentication.repository.UserRepository;
import com.authapp.athentication.service.Imp.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class GoogleAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;
    private final ObjectMapper objectMapper;

    @Value("${jwt.expire-refrash-token}")
    private int refreshTokenExpiration;

    public GoogleAuthenticationSuccessHandler(
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            CookieService cookieService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.cookieService = cookieService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // 1. Get authenticated Google user
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        // 2. Get email
        String email = oidcUser.getEmail();

        // 3. Find user in our database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + email)
                );

        // 4. Generate access and refresh tokens
        TokenResponseDto tokens =
                refreshTokenService.generateTokens(user);

        // 5. Store refresh token in HttpOnly cookie
        cookieService.attachRefreshCookie(
                response,
                tokens.getRefreshToken(),
                refreshTokenExpiration
        );

        // 6. Return access token in response body
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                new AccessTokenResponseDto(tokens.getAccessToken())
        );
    }
}
