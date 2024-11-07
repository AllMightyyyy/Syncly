package org.zakariafarih.syncly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Extract user attributes based on the provider
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // e.g., google
        Map<String, Object> attributes = oauth2User.getAttributes();

        String providerId;
        String email;
        String name;

        switch (registrationId) {
            case "google":
                providerId = (String) attributes.get("sub");
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                break;
            // Add cases for other providers like GitHub, Facebook, etc.
            default:
                throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }

        // Check if user exists
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(registrationId, providerId);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setEmail(email);
            user.setUsername(name);
        } else {
            // Register new user
            user = User.builder()
                    .username(name)
                    .email(email)
                    .IsEmailVerified(true)
                    .provider(registrationId)
                    .providerId(providerId)
                    .build();
        }

        userRepository.save(user);

        return oauth2User;
    }
}