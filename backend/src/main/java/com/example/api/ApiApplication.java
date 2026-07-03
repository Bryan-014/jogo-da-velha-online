package com.example.api;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.Set;

@SpringBootApplication(scanBasePackages = "com.example.api")
public class ApiApplication {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://127.0.0.1:5500",
            "http://localhost:5500"
    );

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<Filter> corsFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();

        registration.setFilter(new Filter() {
            @Override
            public void doFilter(
                    ServletRequest request,
                    ServletResponse response,
                    FilterChain chain
            ) throws IOException, ServletException {

                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                String origin = httpRequest.getHeader("Origin");
                String method = httpRequest.getMethod();

                if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
                    httpResponse.setHeader("Access-Control-Allow-Origin", origin);
                    httpResponse.setHeader("Vary", "Origin");
                    httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
                    httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                    httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization");
                    httpResponse.setHeader("Access-Control-Max-Age", "3600");
                }

                if ("OPTIONS".equalsIgnoreCase(method)) {
                    httpResponse.setStatus(HttpServletResponse.SC_OK);
                    return;
                }

                chain.doFilter(request, response);
            }
        });

        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE);

        return registration;
    }
}
