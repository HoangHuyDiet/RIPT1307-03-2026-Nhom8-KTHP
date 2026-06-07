package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.ConsultationCreateRequest;
import com.smartfinance.smart_finance_hub.dto.response.ConsultationDTO;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    public ResponseEntity<ConsultationDTO> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ConsultationCreateRequest request) {

        ConsultationDTO dto = consultationService.createRequest(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public ResponseEntity<Page<ConsultationDTO>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {

        Page<ConsultationDTO> page = consultationService.getUserRequests(userDetails.getId(), pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationDTO> detail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        ConsultationDTO dto = consultationService.getDetail(id, userDetails.getId(), false);
        return ResponseEntity.ok(dto);
    }
}
