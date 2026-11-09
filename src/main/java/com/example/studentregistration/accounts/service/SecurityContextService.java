package com.example.studentregistration.accounts.service;

import com.example.studentregistration.accounts.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Store authenticated user in Spring Security's session-backed context.
 * Works with Spring Security 6+ / Spring Framework 7.
 */
@Service
public class SecurityContextService {

    /** Key Spring Security uses to persist the context in the HTTP session. */
    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    /** Put the authenticated user into the current session's SecurityContext. */
    public void login(User user, HttpServletRequest request) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        var auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, authorities
        );
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);

        // Save into the HTTP session so subsequent requests stay authenticated.
        HttpSession session = request.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, ctx);
        session.setAttribute("AUTHENTICATED_USER_ID", user.getId());
        session.setAttribute("AUTHENTICATED_USER_EMAIL", user.getEmail());
        session.setAttribute("AUTHENTICATED_USER_ROLE", user.getRole().name());

        // Also set for the current request.
        SecurityContextHolder.setContext(ctx);
    }

    /** Clear session + context on logout. */
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }
}