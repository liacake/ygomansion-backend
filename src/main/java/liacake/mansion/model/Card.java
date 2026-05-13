package liacake.mansion.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Integer scale;
    private Integer linkval;
    private String attribute;
    private String race;
    private String archetype;

    @Column(length = 500)
    private String effect;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "card_linkmarkers", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name =   "linkmarker")
    private List<String> linkmarkers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
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

    public Integer getScale() { return scale; }
    public void setScale(Integer scale) { this.scale = scale; }

    public Integer getLinkval() { return linkval; }
    public void setLinkval(Integer linkval) { this.linkval = linkval; }

    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }

    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }

    public String getArchetype() { return archetype; }
    public void setArchetype(String archetype) { this.archetype = archetype; }

    public String getEffect() { return effect; }
    public void setEffect(String desc) { this.effect = desc; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getLinkmarkers() { return linkmarkers; }
    public void setLinkmarkers(List<String> linkmarkers) { this.linkmarkers = linkmarkers; }

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
}
