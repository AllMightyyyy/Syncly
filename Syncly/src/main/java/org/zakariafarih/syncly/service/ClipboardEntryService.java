package org.zakariafarih.syncly.service;

import org.zakariafarih.syncly.model.ClipboardEntry;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.payload.ClipboardMessage;
import org.zakariafarih.syncly.repository.ClipboardEntryRepository;
import org.zakariafarih.syncly.repository.UserRepository;
import org.zakariafarih.syncly.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing clipboard entries.
 */
@Service
public class ClipboardEntryService {

    @Autowired
    private ClipboardEntryRepository clipboardEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Saves a clipboard entry for a user.
     *
     * @param username the username of the user
     * @param content the content of the clipboard entry
     * @param deviceInfo information about the device
     * @return the saved ClipboardEntry
     */
    @Transactional
    public ClipboardEntry saveClipboardEntry(String username, String content, String deviceInfo) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String encryptedContent = encryptionUtil.encrypt(content);

        ClipboardEntry entry = ClipboardEntry.builder()
                .user(user)
                .content(encryptedContent)
                .deviceInfo(deviceInfo)
                .build();

        ClipboardEntry savedEntry = clipboardEntryRepository.save(entry);

        // Prepare ClipboardMessage
        ClipboardMessage message = new ClipboardMessage();
        message.setContent(content); // Send decrypted content
        message.setDeviceInfo(deviceInfo);
        message.setTimestamp(savedEntry.getTimestamp().toString());
        message.setUsername(username);

        // Broadcast to the user's topic
        messagingTemplate.convertAndSend("/topic/clipboard/" + username, message);

        return savedEntry;
    }

    /**
     * Retrieves the clipboard history for a user.
     *
     * @param username the username of the user
     * @param limit the maximum number of entries to retrieve
     * @param offset the offset for pagination
     * @param searchTerm the search term to filter entries
     * @return a page of ClipboardEntry
     */
    public Page<ClipboardEntry> getClipboardHistory(String username, int limit, int offset, String searchTerm) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PageRequest pageRequest = PageRequest.of(offset / limit, limit);

        if (searchTerm == null || searchTerm.isEmpty()) {
            return clipboardEntryRepository.findByUserAndIsDeletedFalse(user, pageRequest);
        } else {
            return clipboardEntryRepository.findByUserAndContentContainingAndIsDeletedFalse(user, searchTerm, pageRequest);
        }
    }

    /**
     * Deletes a clipboard entry for a user.
     *
     * @param username the username of the user
     * @param entryId the ID of the clipboard entry to delete
     */
    @Transactional
    public void deleteClipboardEntry(String username, Long entryId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ClipboardEntry entry = clipboardEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Clipboard entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this entry");
        }

        entry.setIsDeleted(true);
        clipboardEntryRepository.save(entry);
    }
}