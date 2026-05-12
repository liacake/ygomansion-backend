package liacake.mansion.service;

import liacake.mansion.model.User;
import liacake.mansion.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;

    public Optional<User> getUserById(Long id) { return userRepository.findById(id); }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setDescription(updatedUser.getDescription());
            user.setFavouriteCard(updatedUser.getFavouriteCard());
            user.setImageUrl(updatedUser.getImageUrl());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
