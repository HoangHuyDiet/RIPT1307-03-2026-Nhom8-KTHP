package com.smartfinance.smart_finance_hub.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtils {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private int jwtExpirationMs;

  @Value("${jwt.refresh-expiration:604800000}")
  private long jwtRefreshExpirationMs;

  public String generateToken(UserDetails userDetails) {
    return generateToken(userDetails, jwtExpirationMs, "access");
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return generateToken(userDetails, jwtRefreshExpirationMs, "refresh");
  }

  private String generateToken(UserDetails userDetails, long expirationMs, String tokenType) {
    List<String> roles = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .claim("roles", roles)
        .claim("token_type", tokenType)
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + expirationMs))
        .signWith(key(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String getEmailFromJwtToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key()).build()
        .parseClaimsJws(token).getBody().getSubject();
  }

  @SuppressWarnings("unchecked")
  public List<String> getRolesFromJwtToken(String token) {
    Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
        .parseClaimsJws(token).getBody();
    List<String> roles = claims.get("roles", List.class);
    return roles != null ? roles : List.of();
  }

  public boolean isRefreshToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
          .parseClaimsJws(token).getBody();
      return "refresh".equals(claims.get("token_type", String.class));
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
      return true;
    } catch (MalformedJwtException e) {
      log.error("Chuỗi JWT không đúng định dạng: {}", e.getMessage(), e);
    } catch (ExpiredJwtException e) {
      log.error("JWT token đã hết hạn: {}", e.getMessage(), e);
    } catch (UnsupportedJwtException e) {
      log.error("JWT token không được hỗ trợ: {}", e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string bị trống: {}", e.getMessage(), e);
    }
    return false;
  }

  private Key key() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
