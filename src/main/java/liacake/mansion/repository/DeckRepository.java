package liacake.mansion.repository;

import liacake.mansion.model.Deck;
import liacake.mansion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findByOwner(User owner);

    @Query("SELECT d FROM Deck d JOIN d.localcards c WHERE c = :cardId")
    List<Deck> findDecksByLocalCardString(@Param("cardId") String cardId);
}
