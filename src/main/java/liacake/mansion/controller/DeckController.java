package liacake.mansion.controller;

import liacake.mansion.model.Deck;
import liacake.mansion.model.User;
import liacake.mansion.service.DeckService;
import liacake.mansion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import for @PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/decks")
@CrossOrigin(origins = {"http://localhost:4200","http://localhost:5500","http://127.0.0.1:5500"})
public class DeckController {

    @Autowired
    private DeckService deckService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves a list of decks, optionally filtered by user ID.
     * This endpoint is publicly accessible for GET requests (configured in SecurityConfig).
     *
     * @param userId Optional user ID to filter decks by owner.
     * @return ResponseEntity containing a list of matching Decks.
     */
    @GetMapping
    public ResponseEntity<List<Deck>> getDecks(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            return ResponseEntity.ok(deckService.getDecksByUser(user));
        } else {
            return ResponseEntity.ok(deckService.getAllDecks());
        }
    }

    /**
     * Retrieves a single deck by its ID. This endpoint is publicly accessible (configured in SecurityConfig).
     *
     * @param id The ID of the deck to retrieve.
     * @return ResponseEntity containing the Deck if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Deck> getDeckById(@PathVariable Long id) {
        return deckService.getDeckById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Adds a new deck to the system. Requires user authentication.
     * The deck will be associated with the authenticated user.
     *
     * @param deck The Deck object to be added.
     * @param userDetails The details of the authenticated user.
     * @return ResponseEntity containing the saved Deck.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Only authenticated users can add decks
    public ResponseEntity<Deck> addDeck(@RequestBody Deck deck,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        // userDetails null check is implicitly handled by @PreAuthorize("isAuthenticated()")
        User user = userService.findByUsername(userDetails.getUsername());
        deck.setOwner(user);
        Deck saved = deckService.saveDeck(deck);
        return ResponseEntity.ok(saved);
    }

    /**
     * Updates an existing deck. Requires user authentication.
     * Allowed for ADMINs or the deck's owner.
     *
     * @param id The ID of the deck to update.
     * @param updatedDeck The Deck object with the updated details.
     * @param userDetails The details of the authenticated user.
     * @return ResponseEntity containing the updated Deck or various error statuses (403 Forbidden, 404 Not Found, 500 Internal Server Error).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@deckService.getDeckById(#id).isPresent() and " +
            "@deckService.getDeckById(#id).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())")
    public ResponseEntity<Deck> updateDeck(@PathVariable Long id,
                                           @RequestBody Deck updatedDeck,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        // All authentication and authorization checks are handled by @PreAuthorize.
        // Retrieve the User entity for the service call, as it might need it for other logic.
        User user = userService.findByUsername(userDetails.getUsername());

        try {
            Deck saved = deckService.updateDeck(id, updatedDeck, user); // Service no longer needs to perform ownership checks
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            e.printStackTrace(); // Log the error for debugging
            if (e.getMessage().contains("Deck not found")) { // Only check for "not found" now from service
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a deck from the system. Requires user authentication.
     * Allowed for ADMINs or the deck's owner.
     *
     * @param id The ID of the deck to delete.
     * @param userDetails The details of the authenticated user.
     * @return ResponseEntity indicating success (204 No Content) or various error statuses (403 Forbidden, 404 Not Found, 500 Internal Server Error).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@deckService.getDeckById(#id).isPresent() and " +
            "@deckService.getDeckById(#id).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        // All authentication and authorization checks (isAuthenticated, hasRole, ownership)
        // are handled by the @PreAuthorize annotation.
        // If the principal is not authenticated, Spring Security returns 401 UNAUTHORIZED.
        // If the authorization expression evaluates to false, Spring Security returns 403 FORBIDDEN.

        try {
            deckService.deleteDeck(id); // Service no longer needs to perform ownership checks
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            e.printStackTrace(); // Log the error for debugging purposes
            // If the deck was not found *after* the @PreAuthorize check (e.g., race condition),
            // or if other runtime errors occur within the service.
            if (e.getMessage().contains("Deck not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}