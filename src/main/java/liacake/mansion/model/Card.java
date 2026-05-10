package liacake.mansion.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private Integer atk;
    private Integer def;
    private Integer level;
    private String attribute;
    private String race;

    @Column(length = 500)
    private String effect;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getAtk() { return atk; }
    public void setAtk(Integer atk) { this.atk = atk; }
    public Integer getDef() { return def; }
    public void setDef(Integer def) { this.def = def; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }
    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }
    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}
