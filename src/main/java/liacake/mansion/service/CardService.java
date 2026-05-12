package liacake.mansion.service;

import liacake.mansion.model.Card;
import liacake.mansion.model.Deck;
import liacake.mansion.model.User;
import liacake.mansion.repository.CardRepository;
import liacake.mansion.repository.DeckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    @Autowired private CardRepository cardRepository;
    @Autowired private DeckRepository deckRepository;

    public List<Card> getLocalCards(User owner, String nameFilter) {
        if (nameFilter == null || nameFilter.isEmpty()) return cardRepository.findByOwner(owner);
        return cardRepository.findByOwnerAndNameContainingIgnoreCase(owner, nameFilter);
    }

    public List<Card> getLocalCards(String nameFilter) {
        if (nameFilter == null || nameFilter.isEmpty()) return cardRepository.findAll();
        return cardRepository.findByNameContainingIgnoreCase(nameFilter);
    }

    public Card addLocalCard(Card card, User owner) {
        card.setOwner(owner);
        return cardRepository.save(card);
    }

    public Optional<Card> getLocalCardById(Long id) { return cardRepository.findById(id); }

    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + id));
        String cardIdStr = id.toString();
        List<Deck> decks = deckRepository.findDecksByLocalCardString(cardIdStr);
        for (Deck deck : decks) { deck.removeLocalcard(cardIdStr); deckRepository.save(deck); }
        cardRepository.deleteById(id);
    }

    public Card updateCard(Long id, Card updatedCard, User user) {
        Card existing = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + id));
        existing.setName(updatedCard.getName());
        existing.setType(updatedCard.getType());
        existing.setAtk(updatedCard.getAtk());
        existing.setDef(updatedCard.getDef());
        existing.setLevel(updatedCard.getLevel());
        existing.setAttribute(updatedCard.getAttribute());
        existing.setRace(updatedCard.getRace());
        existing.setEffect(updatedCard.getEffect());
        existing.setImageUrl(updatedCard.getImageUrl());
        return cardRepository.save(existing);
    }

    public Card saveCard(Card card) { return cardRepository.save(card); }
}
