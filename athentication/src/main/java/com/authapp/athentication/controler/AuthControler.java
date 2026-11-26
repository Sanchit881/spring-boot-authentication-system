    package com.authapp.athentication.controler;


    import com.authapp.athentication.dtos.*;
    import com.authapp.athentication.entites.User;
    import com.authapp.athentication.exception.InvalidTokenException;
    import com.authapp.athentication.exception.ResourceNotFoundException;
    import com.authapp.athentication.repository.UserRepository;
    import com.authapp.athentication.security.CookieService;
    import com.authapp.athentication.service.AuthService;
    import com.authapp.athentication.service.Imp.RefreshTokenService;
    import jakarta.servlet.http.HttpServletResponse;
    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.oauth2.core.oidc.user.OidcUser;
    import org.springframework.web.bind.annotation.*;


    @RestController
    @RequestMapping("/api/v1/users")
    public class AuthControler {

       private final AuthenticationManager authenticationManager;
       private final UserRepository userRepository;
       private final RefreshTokenService refreshTokenService;
       private final CookieService cookieService;
       private  final AuthService authService;
       @Value("${jwt.expire-refrash-token}")
       private int refreshTokenExpiration;


        public AuthControler(
                AuthenticationManager authenticationManager,
                UserRepository userRepository,
                RefreshTokenService refreshTokenService,
                CookieService cookieService,
                AuthService authservice

        ) {
            this.authenticationManager = authenticationManager;
            this.userRepository = userRepository;
            this.refreshTokenService = refreshTokenService;
            this.cookieService = cookieService;
            this.authService=authservice;
        }

        // LOGIN
        @PostMapping("/login")
        public ResponseEntity<AccessTokenResponseDto> loginUser(
                @RequestBody LoginRequestDto loginRequestDto,
                HttpServletResponse response
        ) {

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getEmail(),
                            loginRequestDto.getPassword()
                    );

            Authentication authentication =
                    authenticationManager.authenticate(authenticationToken);

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found"));

            TokenResponseDto tokens =
                    refreshTokenService.generateTokens(user);

            // Store refresh token inside HttpOnly cookie
            cookieService.attachRefreshCookie(
                    response,
                    tokens.getRefreshToken(),
                    refreshTokenExpiration
            );

            // Return only access token
            return ResponseEntity.ok(
                    new AccessTokenResponseDto(tokens.getAccessToken())
            );
        }

        // REFRESH TOKEN
        @PostMapping("/refresh")
        public ResponseEntity<AccessTokenResponseDto> refreshToken(
                @CookieValue(
                        name = "refreshToken",
                        required = false
                )
                String refreshToken,
                HttpServletResponse response
        ) {

            if (refreshToken == null || refreshToken.isBlank()) {
                throw new InvalidTokenException("Refresh token notfound");
            }

            TokenResponseDto tokens =
                    refreshTokenService.refreshAccessToken(refreshToken);

            // Replace old cookie with new refresh token
            cookieService.attachRefreshCookie(
                    response,
                    tokens.getRefreshToken(),
                    refreshTokenExpiration
            );

            return ResponseEntity.ok(
                    new AccessTokenResponseDto(tokens.getAccessToken())
            );
        }

        @PostMapping("/register")
        public ResponseEntity<ResponseDto> registerUser(
                 @Valid @RequestBody UserDto userDto
        ) {

            ResponseDto response = authService.RegisterUser(userDto);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        }

        @PostMapping("/logout")
        public ResponseEntity<Void> logout(
                @CookieValue(name = "refreshToken", required = false)
                String refreshToken,
                HttpServletResponse response
        ) {

            // Revoke refresh token in database
            if (refreshToken != null && !refreshToken.isBlank()) {
                refreshTokenService.revokeRefreshToken(refreshToken);
            }

            // Remove refresh token cookie from browser
            cookieService.clearRefreshCookie(response);

            return ResponseEntity.noContent().build();
        }

        @GetMapping("/loginwithgoole")
        public String home() {
            return "http://localhost:8080/oauth2/authorization/google";
        }


        @GetMapping("/profile")
        public String profile(@AuthenticationPrincipal OidcUser user) {

            return "Hello " + user.getFullName()
                    + "<br>Email: " + user.getEmail()
                    + "<br>Google ID: " + user.getSubject();
        }


        @PostMapping("/forgot-password")
        public ResponseEntity<String> forgotPassword(
                @Valid @RequestBody ForgotPasswordRequestDto request) {

            authService.forgotPassword(request.getEmail());

            return ResponseEntity.ok()
                    .body( "If an account exists with this email, a reset link has been sent." +
                            " copy that url send next request on that endpoint and new pass and token");
        }

        @PostMapping("/reset-password")
        public ResponseEntity<String> resetPassword(
                @Valid @RequestBody ResetPasswordRequestDto request) {

            authService.resetPassword(
                    request.getToken(),
                    request.getNewPassword()
            );

            return ResponseEntity.ok().body("Password reset successfully");
        }

    }
