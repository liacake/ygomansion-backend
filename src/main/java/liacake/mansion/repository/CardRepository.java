package liacake.mansion.repository;

import liacake.mansion.model.Card;
import liacake.mansion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByOwner(User owner);
    List<Card> findByOwnerAndNameContainingIgnoreCase(User owner, String name);
    Optional<Card> findById(Long id);
    List<Card> findByNameContainingIgnoreCase(String name);

}