package com.example.TriageIQ.Security;

import com.example.TriageIQ.Entity.Enum.AuthProvider;
import com.example.TriageIQ.Entity.Enum.Role;
import com.example.TriageIQ.Entity.User;
import com.example.TriageIQ.Exception.OAuth2AuthenticationProcessingException;
import com.example.TriageIQ.Repository.UserRepository;
import com.example.TriageIQ.Security.OAuth2.OAuth2UserInfo;
import com.example.TriageIQ.Security.OAuth2.OAuth2UserInfoFactory;
import com.example.TriageIQ.Service.UserService.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;
//    private final UserRepository userRepository;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//        String registrationId = userRequest.getClientRegistration().getRegistrationId();
//
//        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
//                registrationId, oAuth2User.getAttributes());
//
//        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
//            throw new OAuth2AuthenticationProcessingException(
//                    "Email not available from " + registrationId + ". Please make it public and try again.");
//        }
//
//        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
//
//        userRepository.findByEmail(userInfo.getEmail())
//                .map(existing -> updateExistingUser(existing, userInfo))
//                .orElseGet(() -> registerNewUser(userInfo, provider));
//
//        return oAuth2User;
//    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationProcessingException(
                    "Email not available from " + registrationId + ". Please make it public and try again.");
        }

        userService.createOrGetGoogleUser(userInfo.getName(), userInfo.getEmail(), userInfo.getId());

        return oAuth2User;
    }

//    private User registerNewUser(OAuth2UserInfo info, AuthProvider provider) {
//        User user = User.builder()
//                .name(info.getName())
//                .email(info.getEmail())
//                .provider(provider)
//                .providerId(info.getId())
//                .avatarUrl(info.getImageUrl())
//                .role(Role.CUSTOMER) // self-registered via Google — never anything higher
//                .enabled(true)
//                .emailVerified(true) // Google already verified this email for us
//                .build();
//        return userRepository.save(user);
//    }
//
//    private User updateExistingUser(User existing, OAuth2UserInfo info) {
//        existing.setName(info.getName());
//        existing.setAvatarUrl(info.getImageUrl());
//
//        if (existing.getProvider() == AuthProvider.LOCAL) {
//            existing.setProvider(AuthProvider.GOOGLE);
//            existing.setProviderId(info.getId());
//            existing.setEmailVerified(true);
//        }
//        return userRepository.save(existing);
//    }
}
