package liacake.mansion.controller;

import liacake.mansion.model.User;
import liacake.mansion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import for @PreAuthorize
import org.springframework.security.core.Authentication; // Keep this for authentication.name
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:4200","http://localhost:5500","http://127.0.0.1:5500"}, allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Retrieves a user profile by ID. This endpoint is publicly accessible.
     *
     * @param id The ID of the user to retrieve.
     * @return ResponseEntity containing the User if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a user's own profile. Requires user authentication.
     * Allowed for ADMINs or the user themselves.
     *
     * @param id The ID of the user whose profile is being updated.
     * @param user The User object with updated details.
     * @param authentication The current authentication object.
     * @return ResponseEntity containing the updated User or various error statuses (403 Forbidden, 404 Not Found, 500 Internal Server Error).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@userService.getUserById(#id).isPresent() and " +
            "@userService.getUserById(#id).get().getUsername() == authentication.name)")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user, Authentication authentication) {
        // All authentication and authorization checks (isAuthenticated, hasRole, ownership)
        // are handled by the @PreAuthorize annotation.

        // Retrieve the existing user to ensure it exists.
        // @PreAuthorize already checks isPresent(), but we need the entity for the service call.
        // Throwing RuntimeException here is fine as it will be caught by Spring's exception handling
        // or the controller's try-catch.
        User existingUser = userService.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // REMOVED: Manual check for authentication == null. Handled by @PreAuthorize.
        // REMOVED: Manual ownership check. Handled by @PreAuthorize.
        // if (!existingUser.getUsername().equals(currentUsername)) {
        //     return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        // }

        try {
            User updated = userService.updateUser(id, user); // Service needs to accept updated user data
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            e.printStackTrace(); // Log the error for debugging
            // For example, if userService.updateUser throws a specific exception for invalid data.
            // You might add more specific error handling here if needed.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}