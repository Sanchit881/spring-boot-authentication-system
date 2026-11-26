package com.authapp.athentication.entites;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Unique ID inside the JWT
    @Column(nullable = false, unique = true)
    private String jti;

    // Which user owns this refresh token?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // When does this refresh token expire?
    @Column(nullable = false)
    private Instant expiryDate;

    // Is this token revoked?
    @Column(nullable = false)
    private boolean revoked = false;
}
