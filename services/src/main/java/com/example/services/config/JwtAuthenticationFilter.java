package com.example.services.config;

import com.example.services.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Step 1: Extract token từ Authorization header
            var token = extractTokenFromRequest(request);
            // Step 2: Validate token

            if (token != null && jwtUtil.validateToken(token)) {
                // Only allow ACCESS token to authenticate requests protected by SecurityConfig
                var tokenType = jwtUtil.getTokenType(token);
                if (!"ACCESS".equals(tokenType)) {
                    log.debug("Skipping authentication for tokenType={}", tokenType);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Step 3: Extract userId từ token
                var userId = jwtUtil.extractUserId(token);
                if (userId == null) {
                    log.warn("Token validated but userId is missing");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Step 4: Create Authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());

                // Step 5: Set Authentication to SecurityContext
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("Invalid token");
            }
        }
        catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


}
