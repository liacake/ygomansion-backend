package liacake.mansion.controller;

import liacake.mansion.model.Card;
import liacake.mansion.model.User;
import liacake.mansion.service.CardService;
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
@RequestMapping("/api/cards")
@CrossOrigin(origins = {"http://localhost:4200","http://localhost:5500","http://127.0.0.1:5500"})
public class CardController {

    @Autowired
    private CardService cardService;
    @Autowired
    private UserService userService;

    /**
     * Retrieves a list of local cards based on various filters.
     * Can filter by owner (via userId) and detailed card attributes.
     * This endpoint is publicly accessible (configured in SecurityConfig).
     *
     * @param name Card name filter (optional).
     * @param userId User ID to filter by owner (optional).
     * @param type Card type filter (optional).
     * @param effect Card effect filter (optional).
     * @param level Card level filter (optional).
     * @param atk Card ATK filter (optional).
     * @param def Card DEF filter (optional).
     * @param scale Card scale filter (optional).
     * @param linkval Card Link Value filter (optional).
     * @param attribute Card attribute filter (optional).
     * @param race Card race filter (optional).
     * @param archetype Card archetype filter (optional).
     * @return ResponseEntity containing a list of matching Cards.
     */
    @GetMapping
    public ResponseEntity<List<Card>> getCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String effect,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer atk,
            @RequestParam(required = false) Integer def,
            @RequestParam(required = false) Integer scale,
            @RequestParam(required = false) Integer linkval,
            @RequestParam(required = false) String attribute,
            @RequestParam(required = false) String race,
            @RequestParam(required = false) String archetype
    ) {
        User user = null;
        if (userId != null) {
            user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        }

        List<Card> cards = cardService.getLocalCardsFiltered(
                user, name, type, effect, level, atk, def, scale, linkval, attribute, race, archetype
        );
        return ResponseEntity.ok(cards);
    }

    /**
     * Adds a new local card to the system. Requires user authentication.
     * The card will be associated with the authenticated user.
     *
     * @param card The Card object to be added.
     * @param userDetails The details of the authenticated user.
     * @return ResponseEntity containing the saved Card.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Only authenticated users can add cards
    public ResponseEntity<Card> addCard(@RequestBody Card card,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        // userDetails null check is implicitly handled by @PreAuthorize("isAuthenticated()")
        User user = userService.findByUsername(userDetails.getUsername());
        Card saved = cardService.addLocalCard(card, user);
        return ResponseEntity.ok(saved);
    }

    /**
     * Retrieves a single local card by its ID. This endpoint is publicly accessible (configured in SecurityConfig).
     *
     * @param id The ID of the card to retrieve.
     * @return ResponseEntity containing the Card if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Card> getCardById(@PathVariable Long id) {
        return cardService.getLocalCardById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a local card from the system. Requires user authentication.
     * Allowed for ADMINs or the card's owner.
     * This operation will also remove the card from all decks it is part of.
     *
     * @param id The ID of the card to delete.
     * @param userDetails The details of the authenticated user.
     * @return ResponseEntity indicating success (204 No Content) or various error statuses (403 Forbidden, 404 Not Found, 500 Internal Server Error).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@cardService.getLocalCardById(#id).isPresent() and " +
            "@cardService.getLocalCardById(#id).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        // All authentication and authorization checks (isAuthenticated, hasRole, ownership)
        // are handled by the @PreAuthorize annotation.
        // If the principal is not authenticated, Spring Security returns 401 UNAUTHORIZED.
        // If the authorization expression evaluates to false, Spring Security returns 403 FORBIDDEN.

        try {
            cardService.deleteCard(id); // Service no longer needs to perform ownership checks
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            e.printStackTrace(); // Log the error for debugging purposes
            // If the card was not found *after* the @PreAuthorize check (e.g., race condition),
            // or if other runtime errors occur within the service.
            if (e.getMessage().contains("Card not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Updates an existing local card. Requires user authentication.
     * Allowed for ADMINs or the card's owner.
     *
     * @param id The ID of the card to update.
     * @param updatedCard The Card object with the updated details.
     * @param userDetails The details of the authenticated user.
     * @return ResponseEntity containing the updated Card or various error statuses (403 Forbidden, 404 Not Found, 500 Internal Server Error).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@cardService.getLocalCardById(#id).isPresent() and " +
            "@cardService.getLocalCardById(#id).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())")
    public ResponseEntity<Card> updateCard(@PathVariable Long id,
                                           @RequestBody Card updatedCard,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        // All authentication and authorization checks are handled by @PreAuthorize.
        // Retrieve the User entity for the service call, as it might need it for other logic.
        User user = userService.findByUsername(userDetails.getUsername());

        try {
            Card saved = cardService.updateCard(id, updatedCard, user); // Service no longer needs to perform ownership checks
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            e.printStackTrace(); // Log the error for debugging
            if (e.getMessage().contains("Card not found")) { // Only check for not found now from service
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint to replace all occurrences of an old local card with a new one in all decks,
     * and then delete the old card. Requires user authentication.
     * Allowed for ADMINs or if the user owns both the old and the new card.
     *
     * @param oldCardId The ID of the card to be replaced.
     * @param newCardId The ID of the card to replace with.
     * @param userDetails The authenticated user's details.
     * @return ResponseEntity indicating success (204 No Content) or failure (400 Bad Request, 403 Forbidden, 404 Not Found, 500 Internal Server Error).
     */
    @PostMapping("/{oldCardId}/replace/{newCardId}")
    @PreAuthorize("hasAuthority('ADMIN') or (" +
            // Check if old card exists AND user owns it
            "(@cardService.getLocalCardById(#oldCardId).isPresent() and " +
            "@cardService.getLocalCardById(#oldCardId).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId()) and " +
            // Check if new card exists AND user owns it
            "(@cardService.getLocalCardById(#newCardId).isPresent() and " +
            "@cardService.getLocalCardById(#newCardId).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())" +
            ")")
    public ResponseEntity<Void> replaceCard(
            @PathVariable Long oldCardId,
            @PathVariable Long newCardId,
            @AuthenticationPrincipal UserDetails userDetails) {

        // All authorization is handled by @PreAuthorize.
        // No explicit user/ownership checks needed here.
        try {
            // Service handles card existence and invalid arguments (e.g., oldCardId == newCardId)
            cardService.replaceLocalCardInAllDecksAndDeleteOld(oldCardId, newCardId);
            return ResponseEntity.noContent().build(); // 204 No Content for successful operation

        } catch (IllegalArgumentException e) {
            // Catch specific IllegalArgumentException from service (e.g., oldCardId == newCardId)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException e) {
            e.printStackTrace(); // Log the error for debugging on the server side
            // This catch block handles potential RuntimeExceptions from the service,
            // such as "Card not found" if there's a race condition or an unhandled edge case.
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Specific error for card not found
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Generic internal server error
        }
    }
}