package com.authapp.athentication.service.Imp;


import com.authapp.athentication.dtos.ResponseDto;
import com.authapp.athentication.dtos.UserDto;
import com.authapp.athentication.entites.PasswordResetToken;
import com.authapp.athentication.entites.Provider;
import com.authapp.athentication.entites.Role;
import com.authapp.athentication.entites.User;
import com.authapp.athentication.exception.ResourceNotFoundException;
import com.authapp.athentication.exception.UserAlreadyExistsException;
import com.authapp.athentication.repository.PasswordResetTokenRepository;
import com.authapp.athentication.repository.RoleRepository;
import com.authapp.athentication.repository.UserRepository;
import com.authapp.athentication.service.AuthService;
import com.authapp.athentication.service.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthServiceImp implements AuthService {
    private final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private   final PasswordResetTokenService passwordResetTokenService;
    private  final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;


    public AuthServiceImp(UserService userService, UserRepository userService1, UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, ModelMapper modelMapper, PasswordResetTokenService passwordResetTokenService, PasswordResetTokenRepository passwordResetTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.passwordResetTokenService = passwordResetTokenService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public ResponseDto RegisterUser(UserDto userDto) {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }
        User user = modelMapper.map(userDto, User.class);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->
                        new ResourceNotFoundException("ROLE_USER role not found"));

        user.getRoles().add(userRole);
        user.setEnable(true);
        user.setProvider(Provider.LOCAL);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, ResponseDto.class);
    }

    @Override
    public void forgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("user with this email id not present");
        }

        User user = optionalUser.get();

        // Generate original token
        String rawToken = passwordResetTokenService.generateToken();

        System.out.println("row token:" + rawToken);

        // Hash the token
        String tokenHash = passwordResetTokenService.hashToken(rawToken);

        System.out.println("hash token :"+ tokenHash);
        // Create reset token entity
        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setUser(user);
        resetToken.setTokenHash(tokenHash);
        resetToken.setExpiresAt(Instant.now().plusSeconds(15 * 60));
        resetToken.setUsed(false);

        // Save hashed token
        passwordResetTokenRepository.save(resetToken);

        // Send original token through email
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                rawToken
        );

        // Temporary: print only to test your flow
        // Do not log the raw token in production.
    }
    @Transactional
    @Override
    public void resetPassword(String token, String newPassword) {
        // 1. Hash the raw token received from Postman
        String tokenHash = passwordResetTokenService.hashToken(token);

        // 2. Find the reset token in the database
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        // 3. Check whether token has already been used
        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token already used");
        }
        // 4. Check whether token has expired
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Reset token has expired");
        }
        // 5. Get the user associated with this token
        User user = resetToken.getUser();

        // 6. Encode the new password using BCrypt
        user.setPassword(passwordEncoder.encode(newPassword));
        // 7. Save the updated password
        userRepository.save(user);

        // 8. Mark token as used
        resetToken.setUsed(true);

        // 9. Save the updated token status
        passwordResetTokenRepository.save(resetToken);

    }


}
