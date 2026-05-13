package liacake.mansion.controller;

import liacake.mansion.model.Card;
import liacake.mansion.model.User;
import liacake.mansion.service.CardService;
import liacake.mansion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "http://localhost:4200")
public class CardController {

    @Autowired private CardService cardService;
    @Autowired private UserService userService;

    @GetMapping
    public ResponseEntity<List<Card>> getCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer atk,
            @RequestParam(required = false) Integer def,
            @RequestParam(required = false) String attribute,
            @RequestParam(required = false) String race) {
        User user = null;
        if (userId != null) user = userService.getUserById(userId).orElseThrow();
        List<Card> cards = (user != null)
                ? cardService.getLocalCards(user, name)
                : cardService.getLocalCards(name);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Card> addCard(@RequestBody Card card,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(cardService.addLocalCard(card, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Card> getCardById(@PathVariable Long id) {
        return cardService.getLocalCardById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@cardService.getLocalCardById(#id).isPresent() and " +
            "@cardService.getLocalCardById(#id).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@cardService.getLocalCardById(#id).isPresent() and " +
            "@cardService.getLocalCardById(#id).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())")
    public ResponseEntity<Card> updateCard(@PathVariable Long id, @RequestBody Card updatedCard,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(cardService.updateCard(id, updatedCard, user));
    }
}
