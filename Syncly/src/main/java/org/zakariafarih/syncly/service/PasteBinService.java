package org.zakariafarih.syncly.service;

import org.zakariafarih.syncly.model.PasteBin;
import org.zakariafarih.syncly.model.User;
import org.zakariafarih.syncly.repository.PasteBinRepository;
import org.zakariafarih.syncly.repository.UserRepository;
import org.zakariafarih.syncly.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing PasteBin entries.
 */
@Service
public class PasteBinService {

    @Autowired
    private PasteBinRepository pasteBinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    /**
     * Creates a new PasteBin entry for a user.
     *
     * @param username the username of the user
     * @param name the name of the PasteBin entry
     * @param content the content of the PasteBin entry
     * @return the created PasteBin entry
     */
    @Transactional
    public PasteBin createPasteBin(String username, String name, String content) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String encryptedContent = encryptionUtil.encrypt(content);

        PasteBin pasteBin = PasteBin.builder()
                .user(user)
                .name(name)
                .content(encryptedContent)
                .build();

        return pasteBinRepository.save(pasteBin);
    }

    /**
     * Retrieves a paginated list of PasteBin entries for a user.
     *
     * @param username the username of the user
     * @param limit the maximum number of entries to retrieve
     * @param offset the offset for pagination
     * @param searchTerm the search term to filter entries
     * @return a page of PasteBin entries
     */
    public Page<PasteBin> getPasteBins(String username, int limit, int offset, String searchTerm) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PageRequest pageRequest = PageRequest.of(offset / limit, limit);

        if (searchTerm == null || searchTerm.isEmpty()) {
            return pasteBinRepository.findByUserAndIsDeletedFalse(user, pageRequest);
        } else {
            return pasteBinRepository.findByUserAndNameContainingAndIsDeletedFalse(user, searchTerm, pageRequest);
        }
    }

    /**
     * Deletes a PasteBin entry for a user.
     *
     * @param username the username of the user
     * @param pasteBinId the ID of the PasteBin entry to delete
     */
    @Transactional
    public void deletePasteBin(String username, Long pasteBinId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasteBin pasteBin = pasteBinRepository.findById(pasteBinId)
                .orElseThrow(() -> new RuntimeException("Paste bin not found"));

        if (!pasteBin.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this paste bin");
        }

        pasteBin.setIsDeleted(true);
        pasteBinRepository.save(pasteBin);
    }
}