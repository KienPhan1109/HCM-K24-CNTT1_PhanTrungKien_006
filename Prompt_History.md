# NHẬT KÝ TƯƠNG TÁC AI VÀ ĐIỀU HƯỚNG BỐI CẢNH (PROMPT HISTORY & CONTEXT MANAGEMENT)
## DỰ ÁN: PHÊ DUYỆT HỒ SƠ VAY TÍN CHẤP - COREBANKING SYSTEM

---

## 📋 GIỚI THIỆU CHUNG
Tài liệu này ghi lại toàn bộ chuỗi Prompts được chuẩn hóa theo khung cấu trúc **CRICEO** (Context - Role - Instruction/CoT - Constraints - Examples - Output Format) nhằm điều hướng AI Agent thực thi dự án theo đúng quy trình phân chẻ nhiệm vụ (Context Management).

---

## 🔹 PROMPT 1: PHÂN TÍCH & LẬP TÀI LIỆU ĐẶC TẢ YÊU CẦU SRS (TASK 1)

```markdown
[C - CONTEXT]
Dự án CoreBanking (Java Spring Boot, Gradle) đang được triển khai số hóa quy trình vay vốn. Khách hàng cá nhân cần tạo yêu cầu vay tín chấp, và Ngân hàng cần tự động hóa công đoạn kiểm soát rủi ro khi Admin bấm duyệt hồ sơ. Cần lập tài liệu SRS.md chuẩn PTIT tại thư mục gốc repository.

[R - ROLE]
Bạn đóng vai trò là Principal System Architect & SRS Lead Analyst (chuẩn Skill srs-architect & database-architect).

[I - INSTRUCTION & CHAIN-OF-THOUGHT]
Hãy thực hiện các bước suy luận CoT trước khi sinh tài liệu:
1. Phân tích thực thể Customer hiện tại và thiết kế 2 trường tín dụng: `creditScore` (Integer, default 650) và `badDebtStatus` (Boolean, default false).
2. Thiết kế thực thể `LoanApplication` (Amount, Status, RejectionReason, Timestamps) và quan hệ @ManyToOne với Customer.
3. Thiết lập sơ đồ ERD dạng Mermaid, Bảng Từ điển CSDL 4 cột.
4. Lập thuật toán Pseudo-code chi tiết từng bước kiểm tra điều kiện (Điểm < 600 hoặc Nợ xấu = true -> Từ chối vay & Ném HTTP 406).
5. Thiết lập Bảng đặc tả RESTful API 6 cột và Quy định Xử lý Lỗi HTTP Status Codes.

[C - CONSTRAINTS]
- Bắt buộc tạo file SRS.md tại thư mục gốc dự án CoreBanking/SRS.md.
- 100% Bảng CSDL trình bày dạng 4 cột: Tên trường, Kiểu dữ liệu, Ràng buộc, Mô tả chi tiết.
- 100% thuật toán Pseudo-code sử dụng cú pháp chuẩn học thuật, thể hiện rõ luồng ném Exception HTTP 406.

[E - EXAMPLES]
Cấu trúc Pseudo-code mẫu:
IF customer.creditScore < 600 THEN
    loan.status = "REJECTED"; loan.rejectionReason = "...";
    RAISE CreditScoreInsufficientException("Duyệt hồ sơ thất bại: Điểm tín dụng không đủ");
END IF

[O - OUTPUT FORMAT]
Bản tài liệu Markdown hoàn chỉnh với 5 chương đầy đủ theo đúng mẫu đặc tả SRS PTIT.
```

---

## 🔹 PROMPT 2: THIẾT KẾ CUSTOM EXCEPTIONS & JPA ENTITIES (TASK 2.1 - 2.3)

```markdown
[C - CONTEXT]
Hệ thống cần các lớp Custom Exception riêng biệt để bắt lỗi vi phạm điều kiện duyệt vay tín chấp và các JPA Entity ánh xạ chính xác với CSDL Relational (MySQL/H2).

[R - ROLE]
Bạn đóng vai trò là Senior Java Spring Boot Backend Architect (chuẩn Skill springboot-backend-architect).

[I - INSTRUCTION & CHAIN-OF-THOUGHT]
Hãy suy luận từng bước và xây dựng mã nguồn:
1. Tạo 3 lớp Custom Exception kế thừa từ `BusinessException`:
   - `CreditScoreInsufficientException` (gán mã lỗi HTTP 406 Not Acceptable).
   - `BadDebtCustomerException` (gán mã lỗi HTTP 406 Not Acceptable).
   - `LoanApplicationNotFoundException` (gán mã lỗi HTTP 404 Not Found).
2. Cập nhật `Customer.java`: Thêm `creditScore` (mặc định 650) và `badDebtStatus` (mặc định false).
3. Tạo Enum `LoanStatus.java` (PENDING, APPROVED, REJECTED).
4. Tạo Entity `LoanApplication.java` có JPA annotations chuẩn (`@Entity`, `@Table`, `@ManyToOne`, `@JoinColumn`, `@PrePersist`, `@PreUpdate`).

[C - CONSTRAINTS]
- RÀNG BUỘC NGHIÊM NGẶT: Không sử dụng `@Data` Lombok trên Entity (dùng `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`).
- Sử dụng `@Builder.Default` cho `creditScore = 650` và `badDebtStatus = false`.
- Đảm bảo giữ nguyên các thuộc tính sẵn có trong Customer.java.

[E - EXAMPLES]
Mẫu Custom Exception:
public class CreditScoreInsufficientException extends BusinessException {
    public CreditScoreInsufficientException(String message) { super(406, message); }
}

[O - OUTPUT FORMAT]
Các tệp mã nguồn Java hoàn chỉnh: `CreditScoreInsufficientException.java`, `BadDebtCustomerException.java`, `LoanApplicationNotFoundException.java`, `Customer.java`, `LoanStatus.java`, `LoanApplication.java`.
```

---

## 🔹 PROMPT 3: THIẾT KẾ REPOSITORIES, DTOS VÀ SERVICE DUYỆT VAY (TASK 2.4 - 2.7)

```markdown
[C - CONTEXT]
Cần triển khai tầng Data Access (Repository), Data Transfer Object (DTO) và Business Service giải quyết bài toán phê duyệt hồ sơ vay tín chấp tự động.

[R - ROLE]
Bạn đóng vai trò là Enterprise Backend Engineer & Risk Assessment Specialist.

[I - INSTRUCTION & CHAIN-OF-THOUGHT]
Hãy suy luận từng bước cho logic nghiệp vụ `LoanApplicationService`:
1. Tạo `LoanApplicationRepository` kế thừa `JpaRepository<LoanApplication, Long>`.
2. Tạo `CreateLoanRequest` DTO (validate `customerId` not null, `amount` >= 100,000 VND) và `LoanApplicationResponse` DTO.
3. Trong `LoanApplicationService.approveLoanApplication(Long loanId)`:
   - Bước 3.1: Tìm LoanApplication theo loanId. Nếu không thấy -> ném `LoanApplicationNotFoundException`.
   - Bước 3.2: Kiểm tra trạng thái vay, nếu khác PENDING -> ném `BusinessException(400)`.
   - Bước 3.3: Lấy Customer. Nếu `creditScore < 600` -> Cập nhật loan status = REJECTED, lý do = "Điểm tín dụng không đủ", lưu DB và ném `CreditScoreInsufficientException`.
   - Bước 3.4: Nếu `badDebtStatus == true` -> Cập nhật loan status = REJECTED, lý do = "Khách hàng có nợ xấu", lưu DB và ném `BadDebtCustomerException`.
   - Bước 3.5: Đạt điều kiện -> Cập nhật loan status = APPROVED, lưu DB và trả về Response DTO.
4. Cập nhật `AuthService.java`: Khi đăng ký tài khoản khách hàng mới, set mặc định `creditScore = 650` và `badDebtStatus = false`.

[C - CONSTRAINTS]
- Sử dụng `@Transactional` cho các thao tác ghi dữ liệu.
- Phải cập nhật trạng thái `REJECTED` vào CSDL trước khi ném Exception để lưu vết lịch sử từ chối.

[E - EXAMPLES]
Cấu trúc ném lỗi trong Service:
if (creditScore < 600) {
    loan.setStatus(LoanStatus.REJECTED);
    loanRepository.save(loan);
    throw new CreditScoreInsufficientException("Duyệt hồ sơ thất bại: Điểm tín dụng không đủ");
}

[O - OUTPUT FORMAT]
Mã nguồn Java hoàn chỉnh của `LoanApplicationRepository.java`, `CreateLoanRequest.java`, `LoanApplicationResponse.java`, `LoanApplicationService.java`, `AuthService.java`.
```

---

## 🔹 PROMPT 4: TẠO RESTCONTROLLER VÀ XỬ LÝ EXCEPTION HTTP 406 (TASK 2.8 - 3.1)

```markdown
[C - CONTEXT]
Cần cung cấp RESTful API Endpoints cho giao diện/kiểm thử và cấu hình Global Exception Handler bắt lỗi tín dụng trả về HTTP Status Code 406 (Not Acceptable).

[R - ROLE]
Bạn đóng vai trò là Senior REST API Architect & Security Auditor (chuẩn Skill reviewer).

[I - INSTRUCTION & CHAIN-OF-THOUGHT]
Hãy suy luận từng bước:
1. Tạo `LoanApplicationController` tại `/api/v1/loans`:
   - `POST /api/v1/loans`: Khởi tạo hồ sơ vay.
   - `PUT /api/v1/loans/{id}/approve`: Admin duyệt hồ sơ vay.
   - `GET /api/v1/loans/{id}` & `GET /api/v1/loans`: Truy vấn hồ sơ.
2. Cập nhật `GlobalExceptionHandler.java`:
   - Bắt `CreditScoreInsufficientException` -> Bọc trong `ApiResponse.error(406, message)` với `ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)`.
   - Bắt `BadDebtCustomerException` -> Bọc trong `ApiResponse.error(406, message)` với `ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)`.
3. Cập nhật `SecurityConfig.java`: Thêm `/api/v1/loans/**` vào `permitAll()` để phục vụ kiểm thử API.

[C - CONSTRAINTS]
- HTTP Status Code của Response phải chính xác là 406 (Not Acceptable) cho các lỗi vi phạm tín dụng.
- Cấu trúc JSON trả về tuân thủ chuẩn `ApiResponse<T>` của hệ thống CoreBanking.

[E - EXAMPLES]
Cấu trúc Exception Handler:
@ExceptionHandler(CreditScoreInsufficientException.class)
public ResponseEntity<ApiResponse<Void>> handleCreditScore(CreditScoreInsufficientException ex) {
    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
            .body(ApiResponse.error(HttpStatus.NOT_ACCEPTABLE.value(), ex.getMessage()));
}

[O - OUTPUT FORMAT]
Mã nguồn Java hoàn chỉnh: `LoanApplicationController.java`, `GlobalExceptionHandler.java`, `SecurityConfig.java`.
```

---

## 🔹 PROMPT 5: BIÊN DỊCH VÀ XÁC NHẬN TỔNG THỂ DỰ ÁN (TASK 3.2 - 3.4)

```markdown
[C - CONTEXT]
Dự án đã hoàn thành lập trình Backend và viết tài liệu đặc tả. Cần tiến hành kiểm tra biên dịch bằng Gradle và nghiệm thu danh mục nộp bài.

[R - ROLE]
Bạn đóng vai trò là Academic Project Evaluator & QA Lead (chuẩn Skill academic-project-architect).

[I - INSTRUCTION & CHAIN-OF-THOUGHT]
Hãy suy luận từng bước nghiệm thu:
1. Chạy lệnh `./gradlew compileJava` kiểm tra 0 lỗi biên dịch (Zero Syntax Errors).
2. Rà soát sự có mặt của 2 file tài liệu bắt buộc tại thư mục gốc: `SRS.md` và `Prompt_History.md`.
3. Kiểm tra tính toàn vẹn của mã nguồn cũ (không làm hỏng AuthController, BankAccountController).
4. Lập bảng đánh giá tổng thể theo Rubric 100 điểm PTIT.

[C - CONSTRAINTS]
- Phải đảm bảo dự án biên dịch thành công 100%.
- Báo cáo rõ ràng hướng dẫn push repository lên GitHub công khai.

[O - OUTPUT FORMAT]
Báo cáo nghiệm thu Markdown chi tiết kèm bảng tự chấm điểm 100/100.
```
