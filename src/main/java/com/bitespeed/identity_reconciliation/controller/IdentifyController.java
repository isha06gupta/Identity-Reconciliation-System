package com.bitespeed.identity_reconciliation.controller;

import com.bitespeed.identity_reconciliation.dto.IdentifyRequest;
import com.bitespeed.identity_reconciliation.dto.IdentifyResponse;
import com.bitespeed.identity_reconciliation.service.IdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping
public class IdentifyController {

    private final IdentityService identityService;

    public IdentifyController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping("/identify")
    public ResponseEntity<IdentifyResponse> identify(@RequestBody IdentifyRequest request) {
        try {
            return ResponseEntity.ok(identityService.identify(request));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
    @GetMapping("/")
public String healthCheck() {
    return "BiteSpeed Identity Reconciliation API is running ";
}
}