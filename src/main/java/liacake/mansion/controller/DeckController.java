package liacake.mansion.controller;

import liacake.mansion.model.Deck;
import liacake.mansion.model.User;
import liacake.mansion.service.DeckService;
import liacake.mansion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@CrossOrigin(origins = "http://localhost:4200")
public class DeckController {

    @Autowired private DeckService deckService;
    @Autowired private UserService userService;

    @GetMapping
    public ResponseEntity<List<Deck>> getDecks(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            User user = userService.getUserById(userId).orElseThrow();
            return ResponseEntity.ok(deckService.getDecksByUser(user));
        }
        return ResponseEntity.ok(deckService.getAllDecks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deck> getDeckById(@PathVariable Long id) {
        return deckService.getDeckById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Deck> addDeck(@RequestBody Deck deck,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        deck.setOwner(user);
        return ResponseEntity.ok(deckService.saveDeck(deck));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@deckService.getDeckById(#id).isPresent() and " +
            "@deckService.getDeckById(#id).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())")
    public ResponseEntity<Deck> updateDeck(@PathVariable Long id, @RequestBody Deck updatedDeck,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(deckService.updateDeck(id, updatedDeck, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@deckService.getDeckById(#id).isPresent() and " +
            "@deckService.getDeckById(#id).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }
}
