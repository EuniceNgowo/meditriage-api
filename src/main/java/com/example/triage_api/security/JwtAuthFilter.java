package com.example.triage_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Step 1: Extract JWT token from the request header
            String token = extractTokenFromRequest(request);

            // Step 2: Validate the token
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

                // Step 3: Get the user's email from the token payload
                String email = jwtTokenProvider.getEmailFromToken(token);

                // Step 4: Load the full UserDetails from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                /*
                 * Step 5: Create an Authentication object.
                 *
                 * UsernamePasswordAuthenticationToken represents a completed authentication.
                 * Parameters:
                 *   principal   = the UserDetails object (contains email and roles)
                 *   credentials = null (we don't need the password anymore)
                 *   authorities = the user's roles (e.g. [ROLE_USER])
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                // Attaches request metadata (IP address, session ID) to the auth object
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                /*
                 * Step 6: Store the authentication in the SecurityContext.
                 *
                 * The SecurityContext is thread-local — it exists only for the duration
                 * of this request and is automatically cleared after the response is sent.
                 * Spring Security checks this context on every request to decide
                 * whether the user is authenticated.
                 */
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user: {}", email);
            }

        } catch (Exception e) {
            // Log the error but do NOT throw — let the request continue.
            // Spring Security will reject it with 401 if authentication was not set.
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Always continue the filter chain — let the next filter or controller run
        filterChain.doFilter(request, response);
    }


    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // StringUtils.hasText checks: not null, not empty, not whitespace-only
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // remove "Bearer " prefix
        }

        return null;
    }
}
