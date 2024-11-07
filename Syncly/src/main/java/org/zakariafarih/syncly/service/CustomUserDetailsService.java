package org.zakariafarih.syncly.service;

import org.springframework.security.authentication.DisabledException;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Service class for loading user-specific data.
 * Implements the {@link UserDetailsService} interface provided by Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Locates the user based on the username or email.
     *
     * @param usernameOrEmail the username or email identifying the user whose data is required.
     * @return a fully populated user record (never null)
     * @throws UsernameNotFoundException if the user could not be found or the user has no GrantedAuthority
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)));
        if (!user.isIsActive()) {
            throw new DisabledException("User account is disabled");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                getAuthorities(user)
        );
    }

    /**
     * Retrieves the collection of {@link GrantedAuthority} for the specified user.
     *
     * @param user the user whose authorities are required
     * @return a collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}