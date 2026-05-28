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

  public String generateToken(UserDetails userDetails) {
    List<String> roles = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .claim("roles", roles)
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
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
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }
}