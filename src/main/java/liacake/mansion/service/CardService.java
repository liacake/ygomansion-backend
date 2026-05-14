package liacake.mansion.service;

import liacake.mansion.model.Card;
import liacake.mansion.model.Deck;
import liacake.mansion.model.User;
import liacake.mansion.repository.CardRepository;
import liacake.mansion.repository.DeckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private DeckRepository deckRepository;

    /**
     * Retrieves a list of local cards owned by a specific user, optionally filtered by name.
     *
     * @param owner The user who owns the cards.
     * @param nameFilter An optional string to filter card names by (case-insensitive contains).
     * @return A list of matching local cards.
     */
    public List<Card> getLocalCards(User owner, String nameFilter) {
        if (nameFilter == null || nameFilter.isEmpty()) {
            return cardRepository.findByOwner(owner);
        }
        return cardRepository.findByOwnerAndNameContainingIgnoreCase(owner, nameFilter);
    }

    /**
     * Retrieves a list of all local cards, optionally filtered by name.
     * This version does not filter by owner.
     *
     * @param nameFilter An optional string to filter card names by (case-insensitive contains).
     * @return A list of matching local cards.
     */
    public List<Card> getLocalCards(String nameFilter) {
        if (nameFilter == null || nameFilter.isEmpty()) {
            return cardRepository.findAll();
        }
        return cardRepository.findByNameContainingIgnoreCase(nameFilter);
    }

    /**
     * Adds a new local card to the system, associating it with an owner.
     *
     * @param card The Card entity to be added.
     * @param owner The user who will own this card.
     * @return The saved Card entity.
     */
    public Card addLocalCard(Card card, User owner) {
        card.setOwner(owner);
        return cardRepository.save(card);
    }

    /**
     * Retrieves a local card by its ID.
     *
     * @param id The ID of the card.
     * @return An Optional containing the Card if found, or empty if not.
     */
    public Optional<Card> getLocalCardById(Long id) {
        return cardRepository.findById(id);
    }

    /**
     * Deletes a local card from the system. Before deleting the card itself,
     * it removes all references to this card from all decks it is part of.
     * This ensures data consistency with @ElementCollection.
     *
     * @param id The ID of the card to be deleted.
     * @throws RuntimeException if the card is not found.
     */
    @Transactional
    public void deleteCard(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isEmpty()) {
            throw new RuntimeException("Card not found with ID: " + id);
        }

        String cardIdStr = id.toString();

        List<Deck> affectedDecks = deckRepository.findDecksByLocalCardString(cardIdStr);

        for (Deck deck : affectedDecks) {
            deck.removeLocalcard(cardIdStr);
            deckRepository.save(deck);
        }
        cardRepository.deleteById(id);
    }

    /**
     * Replaces all occurrences of a specific local card (oldCardId) with another local card (newCardId)
     * in all decks where oldCardId is present. This method maintains the "quantity" represented
     * by multiple entries in the List<String> by replacing each instance.
     * After successful replacement, the oldCard is deleted from the system.
     *
     * @param oldCardId The ID of the local card to be replaced.
     * @param newCardId The ID of the local card to replace with.
     * @throws IllegalArgumentException if oldCardId and newCardId are the same.
     * @throws RuntimeException if either the old card or the new card is not found.
     */
    @Transactional
    public void replaceLocalCardInAllDecksAndDeleteOld(Long oldCardId, Long newCardId) {
        if (oldCardId.equals(newCardId)) {
            throw new IllegalArgumentException("Old card ID and new card ID cannot be the same for replacement.");
        }

        // Fetch the card entities to ensure they exist
        Optional<Card> optionalOldCard = cardRepository.findById(oldCardId);
        if (optionalOldCard.isEmpty()) {
            throw new RuntimeException("Old card to replace not found with ID: " + oldCardId);
        }
        Optional<Card> optionalNewCard = cardRepository.findById(newCardId);
        if (optionalNewCard.isEmpty()) {
            throw new RuntimeException("New card for replacement not found with ID: " + newCardId);
        }

        String oldCardIdStr = oldCardId.toString();
        String newCardIdStr = newCardId.toString();

        // Find all decks that contain the old local card using the efficient custom query.
        List<Deck> affectedDecks = deckRepository.findDecksByLocalCardString(oldCardIdStr);

        for (Deck deck : affectedDecks) {
            List<String> currentLocalCards = deck.getLocalcards();
            if (currentLocalCards == null) {
                continue;
            }

            List<String> updatedLocalCards = new ArrayList<>();
            for (String cardIdInDeck : currentLocalCards) {
                if (cardIdInDeck.equals(oldCardIdStr)) {
                    updatedLocalCards.add(newCardIdStr);
                } else {
                    updatedLocalCards.add(cardIdInDeck);
                }
            }

            deck.setLocalcards(updatedLocalCards);
            deckRepository.save(deck);
        }

        cardRepository.deleteById(oldCardId);
    }

    /**
     * Saves a Card entity to the database.
     *
     * @param card The Card entity to save.
     * @return The saved Card entity.
     */
    public Card saveCard(Card card) {
        return cardRepository.save(card);
    }

    /**
     * Updates an existing local card. Ownership check is handled at the controller level via @PreAuthorize.
     *
     * @param id The ID of the card to update.
     * @param updatedCard The Card entity with updated details.
     * @param user The user initiating the update (used for context, not ownership check within service).
     * @return The updated Card entity.
     * @throws RuntimeException if the card is not found.
     */
    public Card updateCard(Long id, Card updatedCard, User user) { // User parameter remains for consistency, but not for ownership check
        Card existingCard = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with ID: " + id));

        // REMOVED: Explicit ownership check here. It's now handled by @PreAuthorize in CardController.
        // if (!existingCard.getOwner().getId().equals(user.getId())) {
        //     throw new RuntimeException("not the owner of this card");
        // }

        existingCard.setName(updatedCard.getName());
        existingCard.setType(updatedCard.getType());
        existingCard.setAtk(updatedCard.getAtk());
        existingCard.setDef(updatedCard.getDef());
        existingCard.setLevel(updatedCard.getLevel());
        existingCard.setScale(updatedCard.getScale());
        existingCard.setLinkval(updatedCard.getLinkval());
        existingCard.setAttribute(updatedCard.getAttribute());
        existingCard.setRace(updatedCard.getRace());
        existingCard.setArchetype(updatedCard.getArchetype());
        existingCard.setEffect(updatedCard.getEffect());
        existingCard.setImageUrl(updatedCard.getImageUrl());
        existingCard.setLinkmarkers(updatedCard.getLinkmarkers());

        return cardRepository.save(existingCard);
    }

    /**
     * Filters local cards based on a variety of criteria.
     *
     * @param owner The owner of the cards (optional).
     * @param name Card name filter (case-insensitive contains).
     * @param type Card type filter (exact match).
     * @param effect Card effect description filter (case-insensitive contains).
     * @param level Card level filter (exact match).
     * @param atk Card ATK filter (exact match).
     * @param def Card DEF filter (exact match).
     * @param scale Card scale filter (exact match).
     * @param linkval Card Link Value filter (exact match).
     * @param attribute Card attribute filter (exact match).
     * @param race Card race filter (exact match).
     * @param archetype Card archetype filter (exact match).
     * @return A list of cards matching all specified filters.
     */
    public List<Card> getLocalCardsFiltered(
            User owner,
            String name,
            String type,
            String effect,
            Integer level,
            Integer atk,
            Integer def,
            Integer scale,
            Integer linkval,
            String attribute,
            String race,
            String archetype
    ) {
        List<Card> allCards = (owner != null) ? cardRepository.findByOwner(owner) : cardRepository.findAll();

        return allCards.stream()
                .filter(card -> name == null || card.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(card -> type == null || type.equals(card.getType()))
                .filter(card -> effect == null || (card.getEffect() != null && card.getEffect().toLowerCase().contains(effect.toLowerCase())))
                .filter(card -> level == null || level.equals(card.getLevel()))
                .filter(card -> atk == null || atk.equals(card.getAtk()))
                .filter(card -> def == null || def.equals(card.getDef()))
                .filter(card -> scale == null || scale.equals(card.getScale()))
                .filter(card -> linkval == null || linkval.equals(card.getLinkval()))
                .filter(card -> attribute == null || attribute.equals(card.getAttribute()))
                .filter(card -> race == null || race.equals(card.getRace()))
                .filter(card -> archetype == null || archetype.equals(card.getArchetype()))
                .toList();
    }
}