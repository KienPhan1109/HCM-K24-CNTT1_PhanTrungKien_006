package com.banking.models.dto;

import com.banking.models.constant.LoanStatus;
import com.banking.models.entities.LoanApplication;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Integer creditScore;
    private Boolean badDebtStatus;
    private BigDecimal amount;
    private LoanStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LoanApplicationResponse fromEntity(LoanApplication loan) {
        return LoanApplicationResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer() != null ? loan.getCustomer().getId() : null)
                .customerName(loan.getCustomer() != null ? loan.getCustomer().getFullName() : null)
                .creditScore(loan.getCustomer() != null ? loan.getCustomer().getCreditScore() : null)
                .badDebtStatus(loan.getCustomer() != null ? loan.getCustomer().getBadDebtStatus() : null)
                .amount(loan.getAmount())
                .status(loan.getStatus())
                .rejectionReason(loan.getRejectionReason())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}
