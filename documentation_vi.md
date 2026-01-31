# Tài liệu API và Nghiệp vụ Đánh giá (Evaluation System)

Tài liệu này mô tả chi tiết các API (Endpoint, Input/Output) dành cho đội Frontend (Angular) và Logic nghiệp vụ (Business Rules) dành cho bộ phận BA/Product Ownership.

## 1. Module EvaluationRound (Đợt Đánh giá)

Quản lý chu kỳ của một đợt đánh giá nhân sự.

### 1.1. API Specification (Dành cho Frontend)

Base URL: `/api/evaluation-rounds`

| Method | Endpoint | Mô tả | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/` | Lấy danh sách các đợt đánh giá (không bao gồm mẫu). | - | `APIResponse<List<EvaluationRoundResponse>>` |
| **POST** | `/` | Tạo mới một đợt đánh giá. | `CreateEvaluationRoundRequest` | `APIResponse<EvaluationRoundResponse>` |
| **GET** | `/{roundId}` | Lấy chi tiết một đợt đánh giá. | - | `APIResponse<EvaluationRoundDetailResponse>` |
| **POST** | `/{roundId}/publish` | Công khai (Publish) đợt đánh giá, chuyển trạng thái từ DRAFT sang IN_PROGRESS. | - | `APIResponse<EvaluationRoundResponse>` |
| **POST** | `/{roundId}/auto-assign-leader` | Tự động phân công leader đánh giá cho nhân viên trong đợt. | `RequestParam boolean notify` | `APIResponse<AutoAssignResultResponse>` |
| **GET** | `/{roundId}/manual-assignments` | Lấy danh sách phân công thủ công để chỉnh sửa. | `RequestParam q` (search query) | `APIResponse<List<ManualAssignmentRowResponse>>` |
| **POST** | `/{roundId}/manual-assignments` | Lưu phân công đánh giá thủ công. | `ManualAssignmentRequest` | `APIResponse<AutoAssignResultResponse>` |
| **GET** | `/{roundId}/progress` | Xem tiến độ đánh giá của toàn bộ đợt. | - | `APIResponse<EvaluationRoundProgressResponse>` |
| **GET** | `/{roundId}/summary` | Xem báo cáo tổng kết kết quả đánh giá. | - | `APIResponse<EvaluationRoundSummaryResponse>` |
| **GET** | `/{roundId}/target-candidates` | Lấy danh sách nhân viên có thể được đánh giá trong đợt này. | - | `APIResponse<List<TargetCandidateResponse>>` |
| **POST** | `/{roundId}/targets` | Thêm nhân viên vào danh sách được đánh giá của đợt. | `AddTargetsRequest` | `APIResponse<TargetGenerationResponse>` |
| **GET** | `/evaluators` | Tìm kiếm người đánh giá (Evaluator) potential. | `RequestParam q` | `APIResponse<List<User>>` |

### 1.2. Logic Nghiệp vụ (Service Layer)

#### 1.2.1. Tạo Đợt Đánh giá (`create`)
- **Input**: Tên đợt, loại đợt (Period/Project), thời gian (From/To), dự án (nếu là loại Project), thang điểm, mô tả.
- **Xử lý**:
    - Validate: Thời gian bắt đầu phải trước thời gian kết thúc. Nếu là loại Project thì bắt buộc phải chọn ProjectId.
    - Trạng thái mặc định khi tạo là `DRAFT`.
    - Nếu người dùng chọn sao chép tiêu chí từ đợt khác (`criteriaRoundId`), hệ thống sẽ copy toàn bộ tiêu chí của đợt đó sang đợt mới.

#### 1.2.2. Công khai Đợt Đánh giá (`publish`)
- **Điều kiện**:
    - Đợt đánh giá phải đang ở trạng thái `DRAFT`.
    - Tổng trọng số (Total Weight) của các tiêu chí trong đợt phải bằng đúng **100%**.
- **Kết quả**: Chuyển trạng thái sang `IN_PROGRESS`. Lúc này các phân công đánh giá sẽ bắt đầu có hiệu lực (nếu đã phân công).

#### 1.2.3. Tự động Phân công (`autoAssignLeader`)
- **Mục đích**: Tự động gán người đánh giá là Leader trực tiếp của nhân viên được đánh giá.
- **Logic**:
    - Duyệt danh sách các nhân viên cần được đánh giá (`EvaluationTarget`) trong đợt.
    - Với mỗi nhân viên, tìm Leader trực tiếp (Active) trong bảng `UserLeader`.
    - Nếu tìm thấy Leader và chưa được phân công, hệ thống tạo bản ghi `EvaluationAssignment` với trạng thái `PENDING`.
    - Nếu tham số `notify = true`, gửi thông báo (Notification) đến cho Leader về việc được phân công.

#### 1.2.4. Phân công Thủ công (`manualAssign`)
- **Mục đích**: Cho phép HR/Admin điều chỉnh người đánh giá cho nhân viên.
- **Logic**:
    - Kiểm tra người được chỉ định đánh giá (Evaluator) có nằm trong danh sách Leader (trực tiếp hoặc gián tiếp) hợp lệ của nhân viên đó không.
    - Xóa các phân công cũ không còn trong danh sách yêu cầu mới.
    - Tạo phân công mới cho những người được thêm vào.
    - Gửi thông báo nếu có yêu cầu.

#### 1.2.5. Báo cáo & Tiến độ
- **Tiến độ (`getProgress`)**: Thống kê số lượng phiếu đánh giá Pending, Submitted, Locked. Hiển thị chi tiết từng cặp (Employee - Evaluator).
- **Tổng kết (`getSummary`)**:
  - Tính toán điểm trung bình của từng nhân viên dựa trên các phiếu đánh giá đã nộp.
  - Xếp hạng (Ranking) nhân viên theo điểm số.
  - Phân bố điểm số (Distribution) để vẽ biểu đồ (ví dụ: distribution nhân viên đạt mức điểm A, B, C...).

---

## 2. Module EvaluationRoundCriteria (Tiêu chí Đánh giá)

Quản lý các tiêu chí đánh giá bên trong một đợt cụ thể.

### 2.1. API Specification (Dành cho Frontend)

Base URL: `/api/evaluation-rounds`

| Method | Endpoint | Mô tả | Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/{roundId}/criteria` | Lấy danh sách tiêu chí của đợt. | - | `APIResponse<List<EvaluationRoundCriterionResponse>>` |
| **POST** | `/{roundId}/criteria` | Thêm tiêu chí mới vào đợt. | `CreateEvaluationCriterionRequest` | `APIResponse<EvaluationRoundCriterionResponse>` |
| **PUT** | `/{roundId}/criteria/{crtId}`| Cập nhật tiêu chí trong đợt. | `CreateEvaluationCriterionRequest` | `APIResponse<EvaluationRoundCriterionResponse>` |
| **DELETE**| `/{roundId}/criteria/{crtId}`| Xóa tiêu chí khỏi đợt. | - | `APIResponse<Void>` |
| **GET** | `/criteria-templates` | Lấy danh sách tiêu chí mẫu (Library). | - | `APIResponse<List<EvaluationCriterion>>` |
| **POST** | `/criteria-templates` | Tạo mới tiêu chí mẫu. | `CreateEvaluationCriterionRequest` | `APIResponse<EvaluationCriterion>` |
| **PUT** | `/criteria-templates/{id}` | Cập nhật tiêu chí mẫu. | `CreateEvaluationCriterionRequest` | `APIResponse<EvaluationCriterion>` |
| **DELETE**| `/criteria-templates/{id}` | Xóa tiêu chí mẫu. | - | `APIResponse<Void>` |

### 2.2. Logic Nghiệp vụ (Service Layer)

#### 2.2.1. Quản lý Tiêu chí trong Đợt
- **Thêm/Sửa Tiêu chí (`addCriterion`, `updateCriterion`)**:
    - **Validate**: Tổng trọng số (Weight) của tất cả tiêu chí trong đợt không được vượt quá 100%. Nếu thêm/sửa làm tổng > 100% -> Báo lỗi.
    - **Snapshot**: Khi thêm tiêu chí vào đợt, hệ thống tạo bản ghi `EvaluationRoundCriterion` độc lập với tiêu chí gốc/mẫu. Điều này đảm bảo nếu tiêu chí mẫu thay đổi thì các đợt đánh giá cũ không bị ảnh hưởng.
    - MaxScore > 0.
- **Xóa Tiêu chí**: Xóa khỏi đợt, cập nhật lại tổng trọng số.

#### 2.2.2. Tiêu chí Mẫu (Template Criteria)
- Cho phép quản trị viên tạo sẵn một thư viện các tiêu chí (Ví dụ: "Kỹ năng chuyên môn", "Thái độ làm việc").
- Các tiêu chí này có cờ `isTemplate = true`.

#### 2.2.3. Sao chép Tiêu chí (`applyCriteriaFromRound`)
- Khi tạo đợt mới, có thể chọn copy từ đợt cũ. Hệ thống dẽ duyệt toàn bộ tiêu chí của đợt nguồn và tạo bản sao (New Entity) cho đợt đích.

---

## 3. Module EvaluatorAssignment (Người đánh giá thực hiện)

Dành cho End-user (Người đánh giá) để thực hiện đánh giá nhân viên.

### 3.1. API Specification (Dành cho Frontend)

Base URL: `/api/evaluation-assignments`

| Method | Endpoint | Mô tả | Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/` | Lấy danh sách các bài cần đánh giá của tôi (My Tasks). | `RequestParam status` | `APIResponse<List<EvaluatorAssignmentRowResponse>>` |
| **GET** | `/summary` | Lấy thống kê số lượng bài cần làm (Pending, DueSoon...). | - | `APIResponse<EvaluatorAssignmentSummaryResponse>` |
| **GET** | `/{assignmentId}/form` | Lấy chi tiết form đánh giá (Danh sách tiêu chí + điểm đã lưu). | - | `APIResponse<EvaluationFormResponse>` |
| **POST** | `/{assignmentId}/form` | Lưu nháp (Save Draft) kết quả đánh giá. | `EvaluationFormRequest` | `APIResponse<EvaluationFormResponse>` |
| **POST** | `/{assignmentId}/submit` | Nộp chính thức (Submit) kết quả đánh giá. | `EvaluationFormRequest` | `APIResponse<EvaluationFormResponse>` |

### 3.2. Logic Nghiệp vụ (Service Layer)

#### 3.2.1. Lấy Form Đánh giá (`getEvaluationForm`)
- **Bảo mật**: Chỉ người được phân công (`evaluatorUserId` khớp với current user) mới được xem.
- **Dữ liệu**: Trả về danh sách tiêu chí của đợt đó. Nếu đã từng lưu điểm (nháp), trả về kèm điểm số và ghi chú đã nhập.

#### 3.2.2. Lưu và Nộp Đánh giá (`saveEvaluationForm`)
- **Điều kiện**:
    - User phải là người được phân công.
    - Trạng thái chưa bị `LOCKED` hoặc `CANCELLED`.
- **Validate Điểm**: Điểm nhập vào của từng tiêu chí không được < 0 và không được > MaxScore của tiêu chí đó.
- **Tính toán**: Tổng điểm (`TotalScore`) = Tổng các điểm thành phần.
- **Lưu Nháp (Save Draft)**: Cập nhật điểm vào bảng `EvaluationScore`, cập nhật `EvaluationForm`. Trạng thái giữ nguyên hoặc `PENDING`.
- **Nộp (Submit)**:
    - Lưu điểm như trên.
    - Cập nhật thời gian nộp `SubmittedAt`.
    - Chuyển trạng thái Assignment sang `SUBMITTED`.
    - Sau khi Submit, thông thường sẽ không cho sửa lại (trừ khi có tính năng Reopen - chưa đề cập ở đây).

---

## 4. Module TemplateRound (Đợt Đánh giá Mẫu)

Quản lý các mẫu đợt đánh giá để tái sử dụng cấu hình (Ví dụ: "Mẫu đánh giá thử việc", "Mẫu đánh giá cuối năm").

### 4.1. API Specification (Dành cho Frontend)

Base URL: `/api/evaluation-rounds/templates`

| Method | Endpoint | Mô tả | Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/` | Lấy danh sách các mẫu đợt đánh giá. | - | `APIResponse<List<EvaluationRoundResponse>>` |
| **POST** | `/` | Tạo mới một mẫu đợt đánh giá. | `CreateTemplateRoundRequest` | `APIResponse<EvaluationRoundResponse>` |
| **PUT** | `/{id}` | Cập nhật thông tin mẫu. | `UpdateTemplateRoundRequest` | `APIResponse<EvaluationRoundResponse>` |
| **DELETE**| `/{id}` | Xóa mẫu. | - | `APIResponse<Void>` |

### 4.2. Logic Nghiệp vụ (Service Layer)

#### 4.2.1. Quản lý Mẫu
- **Bản chất**: `TemplateRound` thực chất là một `EvaluationRound` nhưng có cờ `IsTemplateRound = true`.
- **Khác biệt**:
    - Mẫu không có vòng đời (không Publish, không In Progress).
    - Được dùng làm nguồn để Copy/Clone khi tạo đợt đánh giá thật.
- **Xóa Mẫu**: Khi xóa mẫu, hệ thống sẽ xóa kèm theo tất cả các tiêu chí (`EvaluationRoundCriterion`) gắn với mẫu đó để dọn dẹp dữ liệu thừa.
