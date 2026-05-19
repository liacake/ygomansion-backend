package liacake.mansion.service;

import liacake.mansion.model.User;
import liacake.mansion.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import this

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user.
     * @return An Optional containing the User if found, or empty if not.
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user.
     * @return The User entity.
     * @throws UsernameNotFoundException if the user is not found.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Updates an existing user's profile. Ownership check is handled at the controller level via @PreAuthorize.
     *
     * @param id The ID of the user to update.
     * @param updatedUser A User object containing the updated profile information.
     * @return The updated User entity.
     * @throws RuntimeException if the user with the given ID is not found.
     */
    /**
     * Saves a User entity directly (used for image uploads, etc.)
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional // Ensures the entire update operation is atomic
    public User updateUser(Long id, User updatedUser) {
        // The existence check is implicitly performed by findById() and orElseThrow().
        // Ownership/permission check is handled by @PreAuthorize in UserController.
        return userRepository.findById(id).map(user -> {
            user.setDescription(updatedUser.getDescription());
            user.setFavouriteCard(updatedUser.getFavouriteCard());
            user.setImageUrl(updatedUser.getImageUrl());
            // You might add more fields to update here if necessary.
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}