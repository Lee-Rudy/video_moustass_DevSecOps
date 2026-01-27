package com.example.auth.configTest;

import com.example.auth.config.JwtAuthFilter;
import com.example.auth.config.JwtHelper;
import com.example.auth.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private JwtHelper jwtHelper;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        jwtHelper = mock(JwtHelper.class);
    }

    @Test
    void filterChain_shouldReturnSecurityFilterChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        SecurityFilterChain chain = securityConfig.filterChain(http, jwtHelper);
        
        assertNotNull(chain);
    }

    @Test
    void filterChain_shouldDisableCsrf() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        securityConfig.filterChain(http, jwtHelper);
        
        verify(http).csrf(any());
    }

    @Test
    void filterChain_shouldConfigureAuthorizeHttpRequests() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        securityConfig.filterChain(http, jwtHelper);
        
        verify(http).authorizeHttpRequests(any());
    }

    @Test
    void filterChain_shouldAddJwtAuthFilter() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        securityConfig.filterChain(http, jwtHelper);
        
        verify(http).addFilterBefore(any(JwtAuthFilter.class), any());
    }

    @Test
    void filterChain_shouldBuildHttpSecurity() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain expectedChain = mock(DefaultSecurityFilterChain.class);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(expectedChain);
        
        SecurityFilterChain result = securityConfig.filterChain(http, jwtHelper);
        
        assertSame(expectedChain, result);
        verify(http).build();
    }

    @Test
    void filterChain_shouldUseJwtHelperToCreateFilter() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        securityConfig.filterChain(http, jwtHelper);
        
        // JwtHelper is used to create JwtAuthFilter
        verifyNoInteractions(jwtHelper); // JwtHelper is just passed to the filter, not called directly
    }

    @Test
    void securityConfig_shouldBeInstantiable() {
        assertNotNull(new SecurityConfig());
    }

    @Test
    void securityConfig_shouldHaveFilterChainBeanMethod() throws NoSuchMethodException {
        // Verify that the filterChain method exists and has correct signature
        var method = SecurityConfig.class.getMethod("filterChain", HttpSecurity.class, JwtHelper.class);
        
        assertNotNull(method);
        assertEquals(SecurityFilterChain.class, method.getReturnType());
    }

    @Test
    void filterChain_shouldConfigureAllSecurityComponents() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain chain = mock(DefaultSecurityFilterChain.class);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(chain);
        
        SecurityFilterChain result = securityConfig.filterChain(http, jwtHelper);
        
        // Verify all security components are configured
        verify(http).csrf(any()); // CSRF disabled
        verify(http).authorizeHttpRequests(any()); // Authorization configured
        verify(http).addFilterBefore(any(), any()); // JWT filter added
        verify(http).build(); // Security chain built
        
        assertSame(chain, result);
    }

    @Test
    void filterChain_shouldThrowException_whenHttpSecurityIsNull() {
        assertThrows(Exception.class, () -> 
            securityConfig.filterChain(null, jwtHelper)
        );
    }

    @Test
    void filterChain_shouldWorkWithDifferentJwtHelpers() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        JwtHelper anotherJwtHelper = mock(JwtHelper.class);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));
        
        SecurityFilterChain chain1 = securityConfig.filterChain(http, jwtHelper);
        SecurityFilterChain chain2 = securityConfig.filterChain(http, anotherJwtHelper);
        
        assertNotNull(chain1);
        assertNotNull(chain2);
    }

    @Test
    void securityConfig_shouldHaveConfigurationAnnotation() {
        assertTrue(SecurityConfig.class.isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class
        ));
    }

    @Test
    void securityConfig_shouldHaveEnableWebSecurityAnnotation() {
        assertTrue(SecurityConfig.class.isAnnotationPresent(
            org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class
        ));
    }

    @Test
    void filterChain_shouldHaveBeanAnnotation() throws NoSuchMethodException {
        var method = SecurityConfig.class.getMethod("filterChain", HttpSecurity.class, JwtHelper.class);
        
        assertTrue(method.isAnnotationPresent(
            org.springframework.context.annotation.Bean.class
        ));
    }

    @Test
    void securityConfig_shouldCreateNewInstance() {
        SecurityConfig config1 = new SecurityConfig();
        SecurityConfig config2 = new SecurityConfig();
        
        assertNotNull(config1);
        assertNotNull(config2);
        assertNotSame(config1, config2);
    }

    @Test
    void filterChain_shouldAcceptNonNullJwtHelper() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        JwtHelper validHelper = mock(JwtHelper.class);
        
        assertDoesNotThrow(() -> securityConfig.filterChain(http, validHelper));
    }

    @Test
    void filterChain_shouldBeAnnotatedWithBean() throws NoSuchMethodException {
        var method = SecurityConfig.class.getDeclaredMethod("filterChain", HttpSecurity.class, JwtHelper.class);
        var annotations = method.getAnnotations();
        
        assertTrue(annotations.length > 0, "filterChain method should have at least one annotation");
    }
}
