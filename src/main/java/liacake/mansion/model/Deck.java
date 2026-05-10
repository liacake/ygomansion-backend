package liacake.mansion.model;

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
    public List<String> getYgoprocards() { return ygoprocards; }
    public void setYgoprocards(List<String> ygoprocards) { this.ygoprocards = ygoprocards; }
    public List<String> getLocalcards() { return localcards; }
    public void setLocalcards(List<String> localcards) { this.localcards = localcards; }
}
