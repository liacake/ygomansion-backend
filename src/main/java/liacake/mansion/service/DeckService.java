package liacake.mansion.service;

import liacake.mansion.model.Deck;
import liacake.mansion.model.User;
import liacake.mansion.repository.DeckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import this

import java.util.List;
import java.util.Optional;

@Service
public class DeckService {

    @Autowired
    private DeckRepository deckRepository;

    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    public List<Deck> getDecksByUser(User user) {
        return deckRepository.findByOwner(user);
    }

    public Optional<Deck> getDeckById(Long id) {
        return deckRepository.findById(id);
    }

    @Transactional // Good practice for methods that save/modify entities
    public Deck saveDeck(Deck deck) {
        return deckRepository.save(deck);
    }

    @Transactional // Good practice for methods that delete entities
    public void deleteDeck(Long id) {
        // The check for deck existence before deletion is handled by the controller via @PreAuthorize
        // or will result in a RuntimeException if not found, as findById().orElseThrow() implies.
        deckRepository.deleteById(id);
    }

    /**
     * Updates an existing deck. Ownership check is handled at the controller level via @PreAuthorize.
     *
     * @param id The ID of the deck to update.
     * @param updatedDeck The Deck entity with updated details.
     * @param user The user initiating the update (used for context, not ownership check within service).
     * @return The updated Deck entity.
     * @throws RuntimeException if the deck is not found.
     */
    @Transactional // Ensures the entire operation is atomic
    public Deck updateDeck(Long id, Deck updatedDeck, User user) { // User parameter remains for consistency, but not for ownership check
        Deck deck = deckRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Deck not found with ID: " + id));

        // REMOVED: Explicit ownership check here. It's now handled by @PreAuthorize in DeckController.
        // if (!deck.getOwner().getId().equals(user.getId())) {
        //     throw new RuntimeException("You are not the owner of this deck!");
        // }

        deck.setName(updatedDeck.getName());
        deck.setYgoprocards(updatedDeck.getYgoprocards());
        deck.setLocalcards(updatedDeck.getLocalcards());

        return deckRepository.save(deck);
    }
}