package com.banking.controllers;

import com.banking.advice.ApiResponse;
import com.banking.models.dto.CreateLoanRequest;
import com.banking.models.dto.LoanApplicationResponse;
import com.banking.models.services.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loan Applications", description = "Quản lý & Duyệt Hồ sơ Vay Tín chấp (CoreBanking)")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    @Operation(summary = "Khởi tạo hồ sơ vay tín chấp", description = "Khách hàng nộp yêu cầu vay vốn tín chấp với số tiền đề nghị cụ thể.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo hồ sơ vay thành công (PENDING)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ")
    })
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> createLoanApplication(
            @Valid @RequestBody CreateLoanRequest request) {
        LoanApplicationResponse response = loanApplicationService.createLoanApplication(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Khởi tạo hồ sơ vay thành công"),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Duyệt hồ sơ vay tín chấp (Admin)", description = "Tự động kiểm tra Điểm tín dụng (< 600) hoặc Trạng thái nợ xấu (= true). Nếu vi phạm sẽ ném lỗi HTTP 406 Not Acceptable.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Duyệt hồ sơ vay thành công (APPROVED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "406", description = "Từ chối khoản vay: Điểm tín dụng không đủ hoặc có Nợ xấu"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ vay với ID tương ứng")
    })
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> approveLoanApplication(
            @PathVariable("id") Long id) {
        LoanApplicationResponse response = loanApplicationService.approveLoanApplication(id);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Duyệt hồ sơ vay thành công")
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết hồ sơ vay", description = "Truy vấn thông tin chi tiết của một hồ sơ vay theo ID.")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getLoanApplicationById(
            @PathVariable("id") Long id) {
        LoanApplicationResponse response = loanApplicationService.getLoanApplicationById(id);
        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping
    @Operation(summary = "Danh sách hồ sơ vay", description = "Lấy danh sách toàn bộ hồ sơ vay hoặc lọc theo Customer ID.")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getAllLoanApplications(
            @RequestParam(value = "customerId", required = false) Long customerId) {
        List<LoanApplicationResponse> responses = loanApplicationService.getAllLoanApplications(customerId);
        return ResponseEntity.ok(
                ApiResponse.success(responses)
        );
    }
}
