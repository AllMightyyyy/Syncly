package org.zakariafarih.syncly.controller;

import org.zakariafarih.syncly.model.ClipboardEntry;
import org.zakariafarih.syncly.payload.ClipboardDeleteResponse;
import org.zakariafarih.syncly.payload.ClipboardEntryResponse;
import org.zakariafarih.syncly.payload.ClipboardSaveRequest;
import org.zakariafarih.syncly.service.ClipboardEntryService;
import org.zakariafarih.syncly.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/clipboard")
public class ClipboardController {

    @Autowired
    private ClipboardEntryService clipboardEntryService;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @PostMapping("/save")
    public ResponseEntity<?> saveClipboardEntry(@RequestBody ClipboardSaveRequest request, Authentication authentication) {
        String username = authentication.getName();
        ClipboardEntry entry = clipboardEntryService.saveClipboardEntry(username, request.getContent(), request.getDeviceInfo());
        return ResponseEntity.ok("Clipboard entry saved successfully");
    }

    @GetMapping("/history")
    public ResponseEntity<?> getClipboardHistory(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String searchTerm,
            Authentication authentication) {
        String username = authentication.getName();
        Page<ClipboardEntry> entriesPage = clipboardEntryService.getClipboardHistory(username, limit, offset, searchTerm);

        List<ClipboardEntryResponse> responses = entriesPage.getContent().stream().map(entry -> {
            ClipboardEntryResponse response = new ClipboardEntryResponse();
            response.setId(entry.getId());
            response.setContent(encryptionUtil.decrypt(entry.getContent()));
            response.setTimestamp(entry.getTimestamp().toString());
            response.setDeviceInfo(entry.getDeviceInfo());
            return response;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteClipboardEntry(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        clipboardEntryService.deleteClipboardEntry(username, id);
        ClipboardDeleteResponse response = new ClipboardDeleteResponse();
        response.setMessage("Clipboard entry deleted successfully");
        return ResponseEntity.ok(response);
    }
}
