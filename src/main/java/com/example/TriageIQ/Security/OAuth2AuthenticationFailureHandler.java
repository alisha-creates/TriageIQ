package com.example.TriageIQ.Security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend.oauth2-redirect-path}")
    private String oauth2RedirectPath;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {

        String message = exception.getMessage() != null
                ? exception.getMessage()
                : "Google sign-in failed. Please try again.";

        String targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + oauth2RedirectPath)
                .queryParam("error", message)
                .encode()
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
