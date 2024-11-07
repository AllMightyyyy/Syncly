package org.zakariafarih.syncly.controller;

import org.zakariafarih.syncly.model.PasteBin;
import org.zakariafarih.syncly.payload.PasteBinCreateRequest;
import org.zakariafarih.syncly.payload.PasteBinDeleteResponse;
import org.zakariafarih.syncly.payload.PasteBinResponse;
import org.zakariafarih.syncly.service.PasteBinService;
import org.zakariafarih.syncly.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pastebin")
public class PasteBinController {

    @Autowired
    private PasteBinService pasteBinService;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createPasteBin(@RequestBody PasteBinCreateRequest request, Authentication authentication) {
        String username = authentication.getName();
        PasteBin pasteBin = pasteBinService.createPasteBin(username, request.getName(), request.getContent());
        return ResponseEntity.ok("Paste bin created successfully");
    }

    @GetMapping("/list")
    public ResponseEntity<?> listPasteBins(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String searchTerm,
            Authentication authentication) {
        String username = authentication.getName();
        Page<PasteBin> pasteBinsPage = pasteBinService.getPasteBins(username, limit, offset, searchTerm);

        List<PasteBinResponse> responses = pasteBinsPage.getContent().stream().map(pasteBin -> {
            PasteBinResponse response = new PasteBinResponse();
            response.setId(pasteBin.getId());
            response.setName(pasteBin.getName());
            response.setContent(encryptionUtil.decrypt(pasteBin.getContent()));
            response.setTimestamp(pasteBin.getCreatedAt().toString());
            return response;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePasteBin(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        pasteBinService.deletePasteBin(username, id);
        PasteBinDeleteResponse response = new PasteBinDeleteResponse();
        response.setMessage("Paste bin deleted successfully");
        return ResponseEntity.ok(response);
    }
}
