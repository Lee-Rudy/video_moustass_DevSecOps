package com.example.auth.configTest;

import com.example.auth.config.JwtAuthFilter;
import com.example.auth.config.JwtHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtHelper jwtHelper;
    private JwtAuthFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        jwtHelper = mock(JwtHelper.class);
        filter = new JwtAuthFilter(jwtHelper);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        writer = mock(PrintWriter.class);

        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void doFilter_shouldPassThrough_whenMethodIsOptions() throws Exception {
        when(request.getMethod()).thenReturn("OPTIONS");
        when(request.getRequestURI()).thenReturn("/api/orders");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtHelper, never()).parseUserId(anyString());
    }

    @Test
    void doFilter_shouldPassThrough_whenPathIsLogin() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/login");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtHelper, never()).parseUserId(anyString());
    }

    @Test
    void doFilter_shouldPassThrough_whenPathIsInscription() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/inscription/create");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtHelper, never()).parseUserId(anyString());
    }

    @Test
    void doFilter_shouldPassThrough_whenPathNotProtected() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/public/info");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtHelper, never()).parseUserId(anyString());
    }

    @Test
    void doFilter_shouldReturn401_whenTokenMissing() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-Auth-Token")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(401);
        verify(response).setContentType("application/json");
        verify(writer).write(contains("Token manquant ou invalide"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilter_shouldReturn401_whenTokenIsEmpty() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(request.getHeader("X-Auth-Token")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(401);
        verify(writer).write(contains("Token manquant"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilter_shouldSetUserIdAttribute_whenTokenValid() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token-123");
        when(jwtHelper.parseUserId("valid-token-123")).thenReturn(5);

        filter.doFilter(request, response, filterChain);

        verify(request).setAttribute("userId", 5);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(401);
    }

    @Test
    void doFilter_shouldExtractTokenFromBearer() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer my-jwt-token");
        when(jwtHelper.parseUserId("my-jwt-token")).thenReturn(10);

        filter.doFilter(request, response, filterChain);

        verify(jwtHelper).parseUserId("my-jwt-token");
        verify(request).setAttribute("userId", 10);
    }

    @Test
    void doFilter_shouldExtractTokenFromXAuthToken() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/logs");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-Auth-Token")).thenReturn("alternative-token");
        when(jwtHelper.parseUserId("alternative-token")).thenReturn(7);

        filter.doFilter(request, response, filterChain);

        verify(jwtHelper).parseUserId("alternative-token");
        verify(request).setAttribute("userId", 7);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldReturn401_whenTokenInvalid() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtHelper.parseUserId("invalid-token"))
            .thenThrow(new RuntimeException("Invalid JWT"));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(401);
        verify(writer).write(contains("Token invalide ou expiré"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilter_shouldProtectApiUsers() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtHelper.parseUserId("token")).thenReturn(1);

        filter.doFilter(request, response, filterChain);

        verify(jwtHelper).parseUserId("token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldProtectApiOrders() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/orders/123/validate");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtHelper.parseUserId("token")).thenReturn(2);

        filter.doFilter(request, response, filterChain);

        verify(jwtHelper).parseUserId("token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldProtectApiLogs() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/logs");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtHelper.parseUserId("token")).thenReturn(3);

        filter.doFilter(request, response, filterChain);

        verify(jwtHelper).parseUserId("token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldTrimBearerToken() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer   token-with-spaces   ");
        when(jwtHelper.parseUserId("token-with-spaces")).thenReturn(5);

        filter.doFilter(request, response, filterChain);

        verify(jwtHelper).parseUserId("token-with-spaces");
    }

    @Test
    void doFilter_shouldPreferBearerOverXAuthToken() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer bearer-token");
        when(request.getHeader("X-Auth-Token")).thenReturn("x-token");
        when(jwtHelper.parseUserId("bearer-token")).thenReturn(5);

        filter.doFilter(request, response, filterChain);

        verify(jwtHelper).parseUserId("bearer-token");
        verify(jwtHelper, never()).parseUserId("x-token");
    }

    @Test
    void doFilter_shouldSetContentTypeJson_whenUnauthorized() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-Auth-Token")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).setContentType("application/json");
        verify(response).setStatus(401);
    }
}
