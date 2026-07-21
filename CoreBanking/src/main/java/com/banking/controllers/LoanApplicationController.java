package com.banking.controllers;

import com.banking.advice.ApiResponse;
import com.banking.models.dto.CreateLoanRequest;
import com.banking.models.dto.LoanApplicationResponse;
import com.banking.models.services.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> createLoanApplication(
            @Valid @RequestBody CreateLoanRequest request) {
        LoanApplicationResponse response = loanApplicationService.createLoanApplication(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Khởi tạo hồ sơ vay thành công"),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> approveLoanApplication(
            @PathVariable("id") Long id) {
        LoanApplicationResponse response = loanApplicationService.approveLoanApplication(id);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Duyệt hồ sơ vay thành công")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getLoanApplicationById(
            @PathVariable("id") Long id) {
        LoanApplicationResponse response = loanApplicationService.getLoanApplicationById(id);
        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getAllLoanApplications(
            @RequestParam(value = "customerId", required = false) Long customerId) {
        List<LoanApplicationResponse> responses = loanApplicationService.getAllLoanApplications(customerId);
        return ResponseEntity.ok(
                ApiResponse.success(responses)
        );
    }
}
