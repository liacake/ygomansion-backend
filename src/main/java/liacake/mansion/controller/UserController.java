package liacake.mansion.controller;

import liacake.mansion.model.User;
import liacake.mansion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {

    @Autowired private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@userService.getUserById(#id).isPresent() and " +
            "@userService.getUserById(#id).get().getUsername() == authentication.name)")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user, Authentication auth) {
        userService.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        try {
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
