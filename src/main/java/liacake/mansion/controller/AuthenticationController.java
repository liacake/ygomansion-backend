package liacake.mansion.controller;

import liacake.mansion.model.User;
import liacake.mansion.repository.UserRepository;
import liacake.mansion.security.JwtUtil;
import liacake.mansion.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:4200","http://localhost:5500","http://127.0.0.1:5500"})
public class AuthenticationController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private CustomUserDetailsService userDetailsService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public static class AuthRequest {
        public String username;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Incorrect username or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username);
        User user = userRepository.findByUsername(authRequest.username).orElseThrow(() -> new RuntimeException("User not found"));

        // Extract roles from UserDetails and convert to List<String>
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Pass roles as the third argument to generateToken
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), user.getId(), roles);

        Map<String, String> response = new HashMap<>();
        response.put("jwt", jwt);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest authRequest) {
        if (userRepository.findByUsername(authRequest.username).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }

        User newUser = new User();
        newUser.setUsername(authRequest.username);
        newUser.setPassword(passwordEncoder.encode(authRequest.password));
        // Reverted: Assign default role "USER" without "ROLE_" prefix
        newUser.setRoles(Collections.singleton("USER"));

        userRepository.save(newUser);

        // autologin the newly registered user
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username);

        // Extract roles from UserDetails (which now includes the "USER" role)
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Pass roles as the third argument to generateToken
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), newUser.getId(), roles);

        Map<String, String> response = new HashMap<>();
        response.put("jwt", jwt);
        return ResponseEntity.ok(response);
    }

}