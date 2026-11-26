package com.authapp.athentication.config;


import com.authapp.athentication.security.CustomOidcService;
import com.authapp.athentication.security.CustomUserDetailsService;
import com.authapp.athentication.security.GoogleAuthenticationSuccessHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {
    private final CustomOidcService customOidcService;

    public SecurityConfig(CustomOidcService customOidcService) {
        this.customOidcService = customOidcService;
    }

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }


    // Secret Key
    @Bean
    public SecretKey secretKey() {

        byte[] decodedKey =
                Base64.getDecoder().decode(secret);

        return new SecretKeySpec(
                decodedKey,
                "HmacSHA256"
        );
    }


    // JWT Encoder
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {

        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    //decoder
    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer)
        );

        return decoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService userdetailservice ,
                                                       PasswordEncoder passwordencode ){
        DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider(userdetailservice);
        daoAuthenticationProvider.setPasswordEncoder(passwordencode);
        return new ProviderManager(daoAuthenticationProvider);

    }


// Read authorities from JWT

    //Read the roles claim from the JWT.
    //Convert the role values into Spring Security authorities.
    //Don't add any extra prefix because your roles already contain ROLE_.

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName(
                "roles"
        );

        authoritiesConverter.setAuthorityPrefix(" ");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,  GoogleAuthenticationSuccessHandler
            googleAuthenticationSuccessHandler)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())


                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/api/v1/users/register","/api/roles/**","/api/v1/users/refresh", "/api/v1/users/login",
                                        "/api/v1/users/loginwithgoole", "/api/v1/users/forgot-password", "/api/v1/users/reset-password").permitAll()
                                .anyRequest().authenticated()
                )
                //This tells Spring Security:
                //  1. Extract the Bearer token from the request.
                //  2. Validate the JWT using your JwtDecoder.
                //  3. Convert JWT claims into Spring Security authorities.
                //  4. Create an Authentication object.
                //  5. Store authentication in SecurityContextHolder.
                //So Spring Security handles the authentication filter automatically.
                //Therefore, don't register your own JwtAuthenticationFilter as well. You would be implementing overlapping authentication logic

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcService)
                        )
                        .successHandler(googleAuthenticationSuccessHandler)
                );

        return http.build();
    }

}
