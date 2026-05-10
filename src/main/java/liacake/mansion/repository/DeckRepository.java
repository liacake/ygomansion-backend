package liacake.mansion.repository;

import liacake.mansion.model.Deck;
import liacake.mansion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findByOwner(User user);

    // Custom query to find decks containing a specific local card ID string
    // This performs a join on the 'local_cards' element collection table.
    @Query("SELECT DISTINCT d FROM Deck d JOIN d.localcards lc WHERE lc = :cardIdStr")
    List<Deck> findDecksByLocalCardString(@Param("cardIdStr") String cardIdStr);

    // Custom query to find decks containing a specific YGOPRODECK card ID string
    @Query("SELECT DISTINCT d FROM Deck d JOIN d.ygoprocards yc WHERE yc = :cardIdStr")
    List<Deck> findDecksByYgoproCardString(@Param("cardIdStr") String cardIdStr);
}