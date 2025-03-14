package com.openclassrooms.mddapi.security.services;

import com.openclassrooms.mddapi.models.User;
import com.openclassrooms.mddapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Essayer de trouver l'utilisateur par email d'abord (car login utilise email)
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> {
                    // Si pas trouvé par email, essayer par username
                    return userRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException(
                                    "Utilisateur non trouvé avec l'email ou le nom d'utilisateur: " + username));
                });

        return UserDetailsImpl.build(user);
    }
} 