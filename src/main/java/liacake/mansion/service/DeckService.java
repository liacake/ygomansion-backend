package liacake.mansion.service;

import liacake.mansion.model.Deck;
import liacake.mansion.model.User;
import liacake.mansion.repository.DeckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DeckService {

    @Autowired private DeckRepository deckRepository;

    public List<Deck> getAllDecks() { return deckRepository.findAll(); }
    public List<Deck> getDecksByUser(User user) { return deckRepository.findByOwner(user); }
    public Optional<Deck> getDeckById(Long id) { return deckRepository.findById(id); }

    @Transactional
    public Deck saveDeck(Deck deck) { return deckRepository.save(deck); }

    @Transactional
    public void deleteDeck(Long id) { deckRepository.deleteById(id); }

    @Transactional
    public Deck updateDeck(Long id, Deck updatedDeck, User user) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deck not found with ID: " + id));
        deck.setName(updatedDeck.getName());
        deck.setYgoprocards(updatedDeck.getYgoprocards());
        deck.setLocalcards(updatedDeck.getLocalcards());
        return deckRepository.save(deck);
    }
}
