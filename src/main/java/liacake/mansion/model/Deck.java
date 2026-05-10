package liacake.mansion.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "decks")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User owner;

    @ElementCollection
    @CollectionTable(name = "ygoprocard_cards", joinColumns = @JoinColumn(name = "deck_id"))
    @Column(name = "ygoprocard")
    private List<String> ygoprocards;

    @ElementCollection
    @CollectionTable(name = "local_cards", joinColumns = @JoinColumn(name = "deck_id"))
    @Column(name = "localcard")
    private List<String> localcards;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    @JsonProperty("ownerUsername")
    public String getOwnerUsername() {
        return owner != null ? owner.getUsername() : null;
    }

    @JsonProperty("ownerId")
    public Long getOwnerId() {
        return owner != null ? owner.getId() : null;
    }

    @JsonProperty("ownerImage")
    public String getOwnerImage() {
        return owner != null ? owner.getImageUrl() : null;
    }

    public List<String> getYgoprocards() {
        return ygoprocards;
    }

    public void setYgoprocards(List<String> ygoprocards) {
        this.ygoprocards = ygoprocards;
    }

    public List<String> getLocalcards() {
        return localcards;
    }

    public void setLocalcards(List<String> localcards) {
        this.localcards = localcards;
    }

    // Remove a local card by ID string
    public void removeLocalcard(String cardId) {
        if (localcards != null) {
            localcards.removeIf(card -> card.equals(cardId));
        }
    }

    // Optional: Remove a ygopro card as well~
    public void removeYgoprocard(String cardId) {
        if (ygoprocards != null) {
            ygoprocards.removeIf(card -> card.equals(cardId));
        }
    }
}
