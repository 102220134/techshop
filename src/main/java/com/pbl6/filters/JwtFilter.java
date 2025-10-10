package com.pbl6.filters;

import com.pbl6.services.UserService;
import com.pbl6.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.springframework.util.PathMatcher;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!dev")
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    private final PathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> bypassUrls = List.of(
            "/api/public/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/ws/**"
    );

    //api có the khong can login van vao duoc
    private final List<String> optionalSecurityUrls = List.of(
            "/api/checkout/**"
    );

//    private boolean isByPass(HttpServletRequest req) {
//        String ctx = req.getContextPath();
//        String uri = req.getRequestURI();
//        String path = uri.substring(ctx.length());
//
//        return "OPTIONS".equalsIgnoreCase(req.getMethod())
//                || path.startsWith("/uploads/")
//                || bypassUrls.stream().anyMatch(p -> pathMatcher.match(p, path));
//    }

    private boolean matchAny(HttpServletRequest req, List<String> patterns) {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        return patterns.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {

//        if (isByPass(request)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) ||
            matchAny(request, bypassUrls)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        boolean isOptional = matchAny(request, optionalSecurityUrls);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            //Có thể guest-không cần jwt
            if (isOptional) {
                filterChain.doFilter(request, response);
                return;
            }

            authenticationEntryPoint.commence(request, response,
                    new AuthenticationException("Missing or invalid Authorization header") {});
            return;
        }

        String jwtToken = authHeader.substring(7);
        String phone = null;

        try{
            phone = jwtUtil.extractPhone(jwtToken);
        }catch (Exception e){
            authenticationEntryPoint.commence(request, response,
                    new AuthenticationException("Invalid JWT token") {});
            return;
        }

        if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userService.loadUserByPhone(phone);

            if (!jwtUtil.isTokenExpired(jwtToken)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities() // roles
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }else {
                authenticationEntryPoint.commence(request, response,
                        new AuthenticationException("JWT token is expired") {});
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
