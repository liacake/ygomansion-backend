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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
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
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username, req.password));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Incorrect username or password");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.username);
        User user = userRepository.findByUsername(req.username).orElseThrow();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String jwt = jwtUtil.generateToken(userDetails.getUsername(), user.getId(), roles);
        return ResponseEntity.ok(Map.of("jwt", jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        if (userRepository.findByUsername(req.username).isPresent())
            return ResponseEntity.badRequest().body("Username is already taken");
        User newUser = new User();
        newUser.setUsername(req.username);
        newUser.setPassword(passwordEncoder.encode(req.password));
        newUser.setRoles(Collections.singleton("USER"));
        userRepository.save(newUser);
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.username);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String jwt = jwtUtil.generateToken(userDetails.getUsername(), newUser.getId(), roles);
        return ResponseEntity.ok(Map.of("jwt", jwt));
    }
}
