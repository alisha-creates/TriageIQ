package com.example.TriageIQ.Security.OAuth2;

import com.example.TriageIQ.Exception.OAuth2AuthenticationProcessingException;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (!"google".equalsIgnoreCase(registrationId)) {
            throw new OAuth2AuthenticationProcessingException(
                    "Login with " + registrationId + " is not supported");
        }
        return new GoogleOAuth2UserInfo(attributes);
    }
}
