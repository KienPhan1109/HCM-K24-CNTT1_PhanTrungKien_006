package com.banking.models.services;

import com.banking.exceptions.BadDebtCustomerException;
import com.banking.exceptions.BusinessException;
import com.banking.exceptions.CreditScoreInsufficientException;
import com.banking.exceptions.LoanApplicationNotFoundException;
import com.banking.models.constant.LoanStatus;
import com.banking.models.dto.CreateLoanRequest;
import com.banking.models.dto.LoanApplicationResponse;
import com.banking.models.entities.Customer;
import com.banking.models.entities.LoanApplication;
import com.banking.models.repositories.CustomerRepository;
import com.banking.models.repositories.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public LoanApplicationResponse createLoanApplication(CreateLoanRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException(404, "Customer not found with ID: " + request.getCustomerId()));

        LoanApplication loanApplication = LoanApplication.builder()
                .customer(customer)
                .amount(request.getAmount())
                .status(LoanStatus.PENDING)
                .build();

        LoanApplication savedLoan = loanApplicationRepository.save(loanApplication);
        return LoanApplicationResponse.fromEntity(savedLoan);
    }

    @Transactional
    public LoanApplicationResponse approveLoanApplication(Long loanId) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Không tìm thấy hồ sơ vay với ID: " + loanId));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessException(400, "Hồ sơ vay đã được xử lý (APPROVED/REJECTED) trước đó");
        }

        Customer customer = loan.getCustomer();
        if (customer == null) {
            throw new BusinessException(404, "Không tìm thấy thông tin khách hàng sở hữu hồ sơ vay");
        }

        Integer creditScore = customer.getCreditScore();
        if (creditScore == null || creditScore < 600) {
            int score = creditScore == null ? 0 : creditScore;
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRejectionReason("Từ chối duyệt: Điểm tín dụng không đủ (" + score + " < 600 điểm)");
            loanApplicationRepository.save(loan);

            throw new CreditScoreInsufficientException("Duyệt hồ sơ thất bại: Điểm tín dụng khách hàng (" + score + ") dưới ngưỡng tối thiểu 600");
        }

        if (Boolean.TRUE.equals(customer.getBadDebtStatus())) {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRejectionReason("Từ chối duyệt: Khách hàng đang có nợ xấu trên hệ thống tín dụng");
            loanApplicationRepository.save(loan);

            throw new BadDebtCustomerException("Duyệt hồ sơ thất bại: Khách hàng đang thuộc danh sách nợ xấu (Bad Debt)");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setRejectionReason(null);
        LoanApplication savedLoan = loanApplicationRepository.save(loan);

        return LoanApplicationResponse.fromEntity(savedLoan);
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationById(Long loanId) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Không tìm thấy hồ sơ vay với ID: " + loanId));
        return LoanApplicationResponse.fromEntity(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAllLoanApplications(Long customerId) {
        List<LoanApplication> loans;
        if (customerId != null) {
            loans = loanApplicationRepository.findByCustomerId(customerId);
        } else {
            loans = loanApplicationRepository.findAll();
        }
        return loans.stream()
                .map(LoanApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
