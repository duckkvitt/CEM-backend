package com.g47.cem.cemauthentication.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.g47.cem.cemauthentication.service.CustomUserDetailsService;
import com.g47.cem.cemauthentication.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    String username = jwtUtil.extractUsername(jwt);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtUtil.validateToken(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authToken = 
                                new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                                );
                            
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            
                            log.debug("Successfully authenticated user: {}", username);
                        } else {
                            log.debug("Invalid JWT token for user: {}", username);
                        }
                    }
                } catch (Exception e) {
                    log.warn("JWT token validation failed: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        
        // Skip JWT processing for public endpoints
        return path.startsWith("/v1/auth/login") ||
               path.startsWith("/v1/auth/register") ||
               path.startsWith("/v1/auth/refresh-token") ||
               path.startsWith("/v1/auth/verify-email") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/actuator/health") ||
               path.equals("/error");
    }
} 