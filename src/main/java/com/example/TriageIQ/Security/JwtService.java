package com.example.TriageIQ.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.TriageIQ.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    private static final String PURPOSE_CLAIM = "purpose";
    private static final String ROLE_CLAIM = "role";
    private static final String USER_ID_CLAIM = "userId";
    private static final String EMAIL_VERIFICATION_PURPOSE = "EMAIL_VERIFICATION";
    private static final String ACCESS_TOKEN_PURPOSE = "ACCESS";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.verification-expiration-ms}")
    private long verificationTokenExpirationMs;

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(USER_ID_CLAIM, user.getId())
                .claim(ROLE_CLAIM, user.getRole().name())
                .claim(PURPOSE_CLAIM, ACCESS_TOKEN_PURPOSE)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        Claims claims = extractAllClaims(token);
        String purpose = claims.get(PURPOSE_CLAIM, String.class);

        return email.equals(userDetails.getUsername())
                && ACCESS_TOKEN_PURPOSE.equals(purpose)
                && !isTokenExpired(token);
    }

    public String generateEmailVerificationToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(PURPOSE_CLAIM, EMAIL_VERIFICATION_PURPOSE)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + verificationTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmailFromVerificationToken(String token) {
        Claims claims = extractAllClaims(token);
        String purpose = claims.get(PURPOSE_CLAIM, String.class);

        if (!EMAIL_VERIFICATION_PURPOSE.equals(purpose)) {
            throw new IllegalArgumentException("This link is not a valid email verification token");
        }
        if (claims.getExpiration().before(new Date())) {
            throw new IllegalStateException("This verification link has expired. Request a new one.");
        }
        return claims.getSubject();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(USER_ID_CLAIM, Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get(ROLE_CLAIM, String.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
