package com.rdm.service;

import com.rdm.dto.LoginRequest;
import com.rdm.dto.LoginResponse;
import com.rdm.dto.UserDTO;
import com.rdm.model.User;
import com.rdm.repository.UserRepository;
import com.rdm.security.JwtTokenProvider;
import com.rdm.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    
    public AuthService(AuthenticationManager authenticationManager, 
                      JwtTokenProvider tokenProvider,
                      UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        return LoginResponse.builder()
                .token(jwt)
                .type("Bearer")
                .user(UserDTO.fromUser(user))
                .build();
    }
    
    public UserDTO getCurrentUser() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return UserDTO.fromUser(userPrincipal.getUser());
    }
}

