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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private Customer validCustomer;
    private Customer lowCreditCustomer;
    private Customer badDebtCustomer;
    private LoanApplication pendingLoan;

    @BeforeEach
    void setUp() {
        validCustomer = Customer.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("nva@gmail.com")
                .creditScore(700)
                .badDebtStatus(false)
                .build();

        lowCreditCustomer = Customer.builder()
                .id(2L)
                .fullName("Tran Van B")
                .email("tvb@gmail.com")
                .creditScore(550)
                .badDebtStatus(false)
                .build();

        badDebtCustomer = Customer.builder()
                .id(3L)
                .fullName("Le Van C")
                .email("lvc@gmail.com")
                .creditScore(750)
                .badDebtStatus(true)
                .build();

        pendingLoan = LoanApplication.builder()
                .id(100L)
                .customer(validCustomer)
                .amount(new BigDecimal("50000000"))
                .status(LoanStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Khởi tạo hồ sơ vay vốn thành công khi Customer tồn tại")
    void createLoanApplication_Success() {
        CreateLoanRequest request = CreateLoanRequest.builder()
                .customerId(1L)
                .amount(new BigDecimal("50000000"))
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(validCustomer));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(pendingLoan);

        LoanApplicationResponse response = loanApplicationService.createLoanApplication(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(LoanStatus.PENDING, response.getStatus());
        verify(customerRepository, times(1)).findById(1L);
        verify(loanApplicationRepository, times(1)).save(any(LoanApplication.class));
    }

    @Test
    @DisplayName("Duyệt hồ sơ vay thành công khi Điểm tín dụng >= 600 và Không nợ xấu")
    void approveLoanApplication_Success() {
        when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLoan));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse response = loanApplicationService.approveLoanApplication(100L);

        assertNotNull(response);
        assertEquals(LoanStatus.APPROVED, response.getStatus());
        assertNull(response.getRejectionReason());
        verify(loanApplicationRepository, times(1)).save(pendingLoan);
    }

    @Test
    @DisplayName("Từ chối duyệt khoản vay và Ném CreditScoreInsufficientException khi Điểm tín dụng < 600")
    void approveLoanApplication_CreditScoreLessThan600_ShouldThrowCreditScoreInsufficientException() {
        LoanApplication lowCreditLoan = LoanApplication.builder()
                .id(101L)
                .customer(lowCreditCustomer)
                .amount(new BigDecimal("30000000"))
                .status(LoanStatus.PENDING)
                .build();

        when(loanApplicationRepository.findById(101L)).thenReturn(Optional.of(lowCreditLoan));

        CreditScoreInsufficientException exception = assertThrows(
                CreditScoreInsufficientException.class,
                () -> loanApplicationService.approveLoanApplication(101L)
        );

        assertTrue(exception.getMessage().contains("550"));
        assertEquals(406, exception.getCode());
        assertEquals(LoanStatus.REJECTED, lowCreditLoan.getStatus());
        assertNotNull(lowCreditLoan.getRejectionReason());
        verify(loanApplicationRepository, times(1)).save(lowCreditLoan);
    }

    @Test
    @DisplayName("Từ chối duyệt khoản vay và Ném BadDebtCustomerException khi Khách hàng có nợ xấu")
    void approveLoanApplication_BadDebtTrue_ShouldThrowBadDebtCustomerException() {
        LoanApplication badDebtLoan = LoanApplication.builder()
                .id(102L)
                .customer(badDebtCustomer)
                .amount(new BigDecimal("40000000"))
                .status(LoanStatus.PENDING)
                .build();

        when(loanApplicationRepository.findById(102L)).thenReturn(Optional.of(badDebtLoan));

        BadDebtCustomerException exception = assertThrows(
                BadDebtCustomerException.class,
                () -> loanApplicationService.approveLoanApplication(102L)
        );

        assertTrue(exception.getMessage().contains("nợ xấu"));
        assertEquals(406, exception.getCode());
        assertEquals(LoanStatus.REJECTED, badDebtLoan.getStatus());
        assertNotNull(badDebtLoan.getRejectionReason());
        verify(loanApplicationRepository, times(1)).save(badDebtLoan);
    }

    @Test
    @DisplayName("Ném LoanApplicationNotFoundException khi Không tìm thấy hồ sơ vay theo ID")
    void approveLoanApplication_LoanNotFound_ShouldThrowLoanApplicationNotFoundException() {
        when(loanApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        LoanApplicationNotFoundException exception = assertThrows(
                LoanApplicationNotFoundException.class,
                () -> loanApplicationService.approveLoanApplication(999L)
        );

        assertEquals(404, exception.getCode());
        verify(loanApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném BusinessException khi Hồ sơ vay đã được xử lý (APPROVED/REJECTED) trước đó")
    void approveLoanApplication_AlreadyProcessed_ShouldThrowBusinessException() {
        LoanApplication approvedLoan = LoanApplication.builder()
                .id(103L)
                .customer(validCustomer)
                .amount(new BigDecimal("50000000"))
                .status(LoanStatus.APPROVED)
                .build();

        when(loanApplicationRepository.findById(103L)).thenReturn(Optional.of(approvedLoan));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> loanApplicationService.approveLoanApplication(103L)
        );

        assertEquals(400, exception.getCode());
        verify(loanApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném ObjectOptimisticLockingFailureException khi phát sinh tranh chấp ghi đồng thời (Concurrency)")
    void approveLoanApplication_OptimisticLockingFailure_ShouldThrowException() {
        when(loanApplicationRepository.findById(100L)).thenReturn(Optional.of(pendingLoan));
        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException("LoanApplication", 100L));

        assertThrows(
                org.springframework.orm.ObjectOptimisticLockingFailureException.class,
                () -> loanApplicationService.approveLoanApplication(100L)
        );
    }
}

