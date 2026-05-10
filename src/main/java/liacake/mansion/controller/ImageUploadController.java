package liacake.mansion.controller;

import liacake.mansion.model.Card;
import liacake.mansion.model.User;
import liacake.mansion.service.CardService;
import liacake.mansion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ImageUploadController {

    @Autowired private CardService cardService;
    @Autowired private UserService userService;

    private static final long MAX_BYTES = 5 * 1024 * 1024; // 5 MB

    /**
     * Upload an image for a card. Stores as a Base64 data URI in imageUrl.
     * Requires the authenticated user to be the card owner or ADMIN.
     */
    @PostMapping("/cards/{id}/image")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@cardService.getLocalCardById(#id).isPresent() and " +
            "@cardService.getLocalCardById(#id).get().getOwner().getId() == @userService.findByUsername(authentication.name).getId())")
    public ResponseEntity<?> uploadCardImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        ResponseEntity<?> validationError = validateImage(file);
        if (validationError != null) return validationError;

        Card card = cardService.getLocalCardById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        try {
            String dataUri = toDataUri(file);
            card.setImageUrl(dataUri);
            Card saved = cardService.saveCard(card);
            return ResponseEntity.ok(Map.of("imageUrl", saved.getImageUrl()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process image");
        }
    }

    /**
     * Upload a profile image for a user. Stores as a Base64 data URI in imageUrl.
     * Requires the authenticated user to be the target user or ADMIN.
     */
    @PostMapping("/users/{id}/image")
    @PreAuthorize("hasAuthority('ADMIN') or " +
            "(@userService.getUserById(#id).isPresent() and " +
            "@userService.getUserById(#id).get().getUsername() == authentication.name)")
    public ResponseEntity<?> uploadUserImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        ResponseEntity<?> validationError = validateImage(file);
        if (validationError != null) return validationError;

        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String dataUri = toDataUri(file);
            user.setImageUrl(dataUri);
            userService.saveUser(user);
            return ResponseEntity.ok(Map.of("imageUrl", dataUri));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process image");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ResponseEntity<?> validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided");
        }
        if (file.getSize() > MAX_BYTES) {
            return ResponseEntity.badRequest().body("File too large (max 5 MB)");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are accepted");
        }
        return null;
    }

    private String toDataUri(MultipartFile file) throws IOException {
        String mime    = file.getContentType();
        byte[] bytes   = file.getBytes();
        String encoded = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mime + ";base64," + encoded;
    }
}
