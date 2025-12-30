package com.example.hoccode.config.filter;

import com.example.hoccode.config.property.RateLimitProperties;
import com.example.hoccode.service.impl.RateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
public class RateLimitFilter implements Filter {
//    private final RateLimiterService rateLimiterService;
//
//    public RateLimitFilter(RateLimiterService rateLimiterService) {
//        this.rateLimiterService = rateLimiterService;
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//        httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
//        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
//        httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
//        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
//
//        if ("POST".equalsIgnoreCase(httpRequest.getMethod()) && httpRequest.getRequestURI().startsWith("/chat")) {
//            if (!rateLimiterService.isAllowed("chat")) {
//                httpResponse.setStatus(429);
//                httpResponse.setContentType("application/json");
//                httpResponse.getWriter().write("{\"code\":429,\"message\":\"Too many chat requests\"}");
//                return;
//            }
//        }
//        chain.doFilter(request, response);
//    }

    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties rateLimitProperties; // Inject cấu hình mới

    // Constructor để tiêm cả hai dependency
    public RateLimitFilter(RateLimiterService rateLimiterService, RateLimitProperties rateLimitProperties) {
        this.rateLimiterService = rateLimiterService;
        this.rateLimitProperties = rateLimitProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
//        System.out.println("httpRequest.getRequestURI(): " + httpRequest.getRequestURI());
//        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
//            chain.doFilter(request, response);
//            return;
//        }

        String method = httpRequest.getMethod().toUpperCase();
        String path = httpRequest.getRequestURI();

        RateLimitProperties.EnforcedEndpoint matchedEndpoint =
                rateLimitProperties.getEnforcedEndpoints().stream()
                        .filter(e ->
                                e.getMethod().equalsIgnoreCase(method)
                                        && e.getPath().equals(path)
                        )
                        .findFirst()
                        .orElse(null);

        if (matchedEndpoint != null) {
            String rateLimitKey = matchedEndpoint.getLimitKey();
//            System.out.println("Rate limit applied: " + rateLimitKey);

            if (!rateLimiterService.isAllowed(rateLimitKey)) {
                httpResponse.setStatus(429);
                Map<String, Object> body = new HashMap<>();
                body.put("code", 429);
                body.put("message", "Truy cập quá nhanh");
                body.put("data", null);

                httpResponse.setContentType("application/json;charset=UTF-8");
                new ObjectMapper().writeValue(httpResponse.getWriter(), body);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}