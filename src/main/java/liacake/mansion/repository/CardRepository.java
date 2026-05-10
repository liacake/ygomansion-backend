package liacake.mansion.repository;

import liacake.mansion.model.Card;
import liacake.mansion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByOwner(User owner);
    List<Card> findByOwnerAndNameContainingIgnoreCase(User owner, String name);
    List<Card> findByNameContainingIgnoreCase(String name);
}
