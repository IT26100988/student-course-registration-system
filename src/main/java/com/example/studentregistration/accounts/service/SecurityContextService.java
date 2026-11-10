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

@Service
public class SecurityContextService {

    public void login(User user, HttpServletRequest request) {
        // ROLE_ කියන prefix එක අනිවාර්යයි
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        var auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, authorities
        );

        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);

        // Spring Security 6/7 වලට අනුව Session එකට save කරන විදිහ
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", ctx);

        // Custom attributes (අපේ පහසුවට)
        session.setAttribute("AUTHENTICATED_USER_ID", user.getId());
        session.setAttribute("AUTHENTICATED_USER_EMAIL", user.getEmail());
        session.setAttribute("AUTHENTICATED_USER_ROLE", user.getRole().name());

        // මේ Request එකට අදාළව Context එක set කරනවා
        SecurityContextHolder.setContext(ctx);
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }
}