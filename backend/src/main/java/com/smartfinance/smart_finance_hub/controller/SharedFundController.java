package com.smartfinance.smart_finance_hub.controller;

import com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest;
import com.smartfinance.smart_finance_hub.dto.request.InviteMemberRequest;
import com.smartfinance.smart_finance_hub.dto.request.RespondInvitationRequest;
import com.smartfinance.smart_finance_hub.entity.FundInvitation;
import com.smartfinance.smart_finance_hub.entity.Transaction;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.SharedFundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class SharedFundController {

    private final SharedFundService sharedFundService;
    private final com.smartfinance.smart_finance_hub.repository.CategoryRepository categoryRepository;
    private final com.smartfinance.smart_finance_hub.repository.UserRepository userRepository;
    private final com.smartfinance.smart_finance_hub.repository.TransactionRepository transactionRepository;
    private final com.smartfinance.smart_finance_hub.service.NotificationService notificationService;

    @GetMapping("/list")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.List<java.util.Map<String, Object>>>> listFunds() {
        try {
            Long userId = getCurrentUserId();
            java.util.List<com.smartfinance.smart_finance_hub.entity.ShareFund> funds = sharedFundService.getFundsForUser(userId);
            
            java.util.List<java.util.Map<String, Object>> responseList = new java.util.ArrayList<>();
            for (com.smartfinance.smart_finance_hub.entity.ShareFund fund : funds) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", fund.getId());
                map.put("name", fund.getName());
                map.put("description", fund.getDescription());
                map.put("balance", fund.getBalance());
                map.put("target", java.math.BigDecimal.valueOf(10000000));
                map.put("status", "ACTIVE".equalsIgnoreCase(fund.getStatus()) ? "active" : "settled");
                
                String[] colors = {"#1A73E8", "#52C41A", "#FA8C16", "#EA4335", "#722ED1"};
                map.put("themeColor", colors[(int)(fund.getId() % colors.length)]);
                
                java.util.List<java.util.Map<String, Object>> membersList = new java.util.ArrayList<>();
                if (fund.getMembers() != null) {
                    for (com.smartfinance.smart_finance_hub.entity.FundMember member : fund.getMembers()) {
                        java.util.Map<String, Object> m = new java.util.HashMap<>();
                        m.put("name", member.getUser().getDisplayName());
                        m.put("email", member.getUser().getEmail());
                        m.put("avatar", "https://api.dicebear.com/7.x/notionists/svg?seed=" + member.getUser().getDisplayName());
                        m.put("role", member.getFundRole());
                        membersList.add(m);
                    }
                }
                map.put("members", membersList);
                map.put("membersCount", membersList.size());
                
                responseList.add(map);
            }
            
            return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Lấy danh sách quỹ thành công!", responseList));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách quỹ nhóm: {}", e.getMessage(), e);
            return new ResponseEntity<>(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.error("Lỗi lấy danh sách quỹ: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/activities")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.List<java.util.Map<String, Object>>>> getActivities() {
        Long userId = getCurrentUserId();
        java.util.List<java.util.Map<String, Object>> activities = sharedFundService.getActivitiesForUser(userId);
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Lấy lịch sử hoạt động thành công!", activities));
    }

    @GetMapping("/my-notifications")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.List<java.util.Map<String, Object>>>> getMyNotifications() {
        Long userId = getCurrentUserId();
        java.util.List<java.util.Map<String, Object>> notifications = notificationService.getMyNotifications(userId);
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Lay thong bao thanh cong!", notifications));
    }

    @PostMapping("/my-notifications/read")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Void>> readNotification(
            @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        Long notifId = parseNotificationId(body.get("id"));
        if (notifId != null) {
            notificationService.markAsRead(notifId, userId);
        }
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Da doc"));
    }

    @PostMapping("/my-notifications/read-all")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Void>> readAllNotifications() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Da doc het"));
    }

    @PostMapping("/my-notifications/delete")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Void>> deleteNotification(
            @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        Long notifId = parseNotificationId(body.get("id"));
        if (notifId != null) {
            notificationService.deleteNotification(notifId, userId);
        }
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Da xoa"));
    }

    @PostMapping("/my-notifications/delete-all")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Void>> deleteAllNotifications() {
        Long userId = getCurrentUserId();
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Da xoa het"));
    }

    private Long parseNotificationId(String idStr) {
        if (idStr == null) return null;
        String clean = idStr;
        int lastUnderScore = idStr.lastIndexOf('_');
        if (lastUnderScore != -1) {
            clean = idStr.substring(lastUnderScore + 1);
        }
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @PostMapping
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.Map<String, Object>>> createFund(
            @RequestBody java.util.Map<String, Object> body) {
        try {
            Long userId = getCurrentUserId();
            String name = (String) body.get("name");
            
            java.math.BigDecimal target = java.math.BigDecimal.ZERO;
            if (body.get("target") != null) {
                target = new java.math.BigDecimal(body.get("target").toString());
            }
            
            java.math.BigDecimal initialContribution = java.math.BigDecimal.ZERO;
            if (body.get("initialContribution") != null) {
                initialContribution = new java.math.BigDecimal(body.get("initialContribution").toString());
            }
            
            com.smartfinance.smart_finance_hub.entity.ShareFund fund = sharedFundService.createFund(name, target, initialContribution, userId);
            com.smartfinance.smart_finance_hub.entity.User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

            java.util.List<java.util.Map<String, Object>> membersList = new java.util.ArrayList<>();
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("name", user.getDisplayName());
            m.put("email", user.getEmail());
            m.put("avatar", "https://api.dicebear.com/7.x/notionists/svg?seed=" + user.getDisplayName());
            m.put("role", "OWNER");
            membersList.add(m);
            
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("id", fund.getId());
            data.put("name", fund.getName());
            data.put("balance", fund.getBalance());
            data.put("target", target);
            data.put("status", "active");
            data.put("themeColor", "#1A73E8");
            data.put("members", membersList);
            data.put("membersCount", 1);
            
            return new ResponseEntity<>(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Tạo quỹ thành công!", data), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Lỗi khi tạo quỹ nhóm: {}", e.getMessage(), e);
            return new ResponseEntity<>(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.error("Lỗi tạo quỹ: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/rename")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.Map<String, Object>>> renameFund(
            @RequestBody java.util.Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Long fundId = Long.valueOf(body.get("fundId").toString());
        String newName = (String) body.get("newName");
        
        com.smartfinance.smart_finance_hub.entity.ShareFund fund = sharedFundService.renameFund(fundId, newName, userId);
        
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", fund.getId());
        data.put("name", fund.getName());
        
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Đổi tên quỹ thành công!", data));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Void>> leaveFund(
            @PathVariable("id") Long fundId) {
        Long userId = getCurrentUserId();
        sharedFundService.leaveFund(fundId, userId);
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Rời khỏi quỹ thành công!"));
    }

    @PostMapping("/{id}/request-delete-fund")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.Map<String, Object>>> requestDeleteFund(
            @PathVariable("id") Long fundId) {
        Long userId = getCurrentUserId();
        String msg = sharedFundService.deleteFund(fundId, userId);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("requestId", "del-req-" + System.currentTimeMillis());
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Void>> deleteFund(
            @PathVariable("id") Long fundId) {
        Long userId = getCurrentUserId();
        String msg = sharedFundService.deleteFund(fundId, userId);
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success(msg));
    }

    @GetMapping("/transactions")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.List<java.util.Map<String, Object>>>> getTransactions(
            @RequestParam("fundId") Long fundId) {
        Long userId = getCurrentUserId();
        java.util.List<com.smartfinance.smart_finance_hub.entity.Transaction> txs = sharedFundService.getFundTransactions(fundId, userId);
        
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (com.smartfinance.smart_finance_hub.entity.Transaction tx : txs) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", tx.getId());
            m.put("type", tx.getType());
            m.put("amount", tx.getAmount());
            m.put("description", tx.getDescription());
            m.put("date", tx.getDate() != null ? tx.getDate().toString() : null);
            m.put("user_id", tx.getUser() != null ? tx.getUser().getId() : null);
            m.put("user_display_name", tx.getUser() != null ? tx.getUser().getDisplayName() : "Nguoi dung");
            m.put("user_email", tx.getUser() != null ? tx.getUser().getEmail() : null);
            m.put("category_name", tx.getCategory() != null ? tx.getCategory().getName() : "Khác");
            m.put("is_approved", Boolean.TRUE.equals(tx.getIsApproved()));
            m.put("status", tx.getStatus() != null ? tx.getStatus() : "PENDING");
            m.put("reject_reason", tx.getRejectReason());
            m.put("bank_account", tx.getBankAccount());
            m.put("bank_name", tx.getBankName());
            list.add(m);
        }
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Lấy danh sách giao dịch thành công!", list));
    }

    @PostMapping("/transaction-request")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.Map<String, Object>>> createTransactionRequest(
            @RequestBody java.util.Map<String, Object> body) {
        
        Long userId = getCurrentUserId();
        Long fundId = Long.valueOf(body.get("fundId").toString());
        String type = (String) body.get("type");
        java.math.BigDecimal amount = new java.math.BigDecimal(body.get("amount").toString());
        String description = (String) body.get("description");
        Long categoryId = body.get("categoryId") != null ? Long.valueOf(body.get("categoryId").toString()) : null;
        Long personalFundId = body.get("personalFundId") != null ? Long.valueOf(body.get("personalFundId").toString()) : null;
        java.time.LocalDate date = body.get("date") != null
                ? java.time.LocalDate.parse(body.get("date").toString())
                : java.time.LocalDate.now();
        
        String normalizedType = type != null ? type.toUpperCase() : null;
        com.smartfinance.smart_finance_hub.entity.Category category = categoryId != null
                ? categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + categoryId))
                : categoryRepository
                    .findSystemCategories(normalizedType)
                    .stream()
                    .findFirst()
                    .orElse(categoryRepository.findSystemCategories(null).stream().findFirst().orElse(null));
                
        if (category == null) {
            throw new IllegalArgumentException("Vui lòng tạo ít nhất một danh mục thu/chi trong hệ thống!");
        }

        com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest req = 
                new com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest(
                        amount, type, description, date, category.getId(), personalFundId);

        com.smartfinance.smart_finance_hub.entity.Transaction tx = 
                sharedFundService.createFundTransaction(fundId, req, userId);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("requestId", tx.getId().toString());
        data.put("isApproved", tx.getIsApproved());
        data.put("status", tx.getStatus());
        
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Gửi yêu cầu giao dịch thành công!", data));
    }

    @GetMapping("/{id}/discussions")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.List<java.util.Map<String, Object>>>> getDiscussions(
            @PathVariable("id") Long fundId) {
        try {
            Long userId = getCurrentUserId();
            com.smartfinance.smart_finance_hub.entity.User currentUser = userRepository.findById(userId).orElse(null);
            String currentUserName = currentUser != null ? currentUser.getDisplayName() : "";

            java.util.List<com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse> discussions = 
                    sharedFundService.getDiscussions(fundId, userId);
            
            java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
            for (com.smartfinance.smart_finance_hub.dto.response.FundDiscussionResponse disc : discussions) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", disc.getId());
                m.put("groupId", fundId);
                m.put("type", disc.getType());
                m.put("senderName", disc.getSenderName());
                m.put("senderAvatar", "https://api.dicebear.com/7.x/notionists/svg?seed=" + disc.getSenderName());
                m.put("text", disc.getText());
                m.put("time", disc.getTime());
                m.put("isMe", currentUserName.equals(disc.getSenderName()));
                list.add(m);
            }
            return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Lấy tin nhắn thảo luận thành công!", list));
        } catch (Exception e) {
            log.error("Lỗi lấy tin nhắn thảo luận: {}", e.getMessage(), e);
            return new ResponseEntity<>(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.error("Lỗi: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/budget-chart")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.List<java.util.Map<String, Object>>>> getBudgetChart(
            @PathVariable("id") Long fundId) {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        String[] months = {"T12", "T1", "T2", "T3", "T4", "Tháng này"};
        int[] amounts = {200000, 150000, 50000, 100000, 50000, 2500000};
        for (int i = 0; i < months.length; i++) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("month", months[i]);
            m.put("amount", amounts[i]);
            list.add(m);
        }
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Lấy biểu đồ ngân sách thành công!", list));
    }

    @PostMapping("/remove-request")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.Map<String, Object>>> removeRequest(
            @RequestBody java.util.Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Long fundId = Long.valueOf(body.get("fundId").toString());
        String memberEmail = (String) body.get("memberEmail");
        
        sharedFundService.removeMember(fundId, memberEmail, userId);
        
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("requestId", "rem-req-" + System.currentTimeMillis());
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Đã xóa thành viên khỏi quỹ thành công!", data));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getId();
        }
        throw new IllegalStateException("Không thể xác thực người dùng hiện tại!");
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Map<String, Object>>> inviteMember(
            @PathVariable("id") Long fundId,
            @Valid @RequestBody InviteMemberRequest request) {

        log.info("inviteMember param: fundId={}, email={}", fundId, request.getEmail());

        Long currentUserId = getCurrentUserId();
        FundInvitation invitation = sharedFundService.inviteMember(fundId, request, currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("invitationId", invitation.getId());
        data.put("invitedEmail", invitation.getInvitedEmail());
        data.put("status", invitation.getStatus());
        data.put("expiresAt", invitation.getExpiresAt().toString());
        data.put("emailSent", true);

        return new ResponseEntity<>(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Đã gửi lời mời thành công đến " + invitation.getInvitedEmail(), data), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Void>> respondToInvitation(
            @PathVariable("id") Long fundId,
            @Valid @RequestBody RespondInvitationRequest request) {

        log.info("respondToInvitation param: fundId={}, invitationId={}, action={}", fundId, request.getInvitationId(), request.getAction());

        Long currentUserId = getCurrentUserId();
        sharedFundService.respondToInvitation(fundId, request, currentUserId);

        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Đã xử lý phản hồi lời mời: " + request.getAction().toUpperCase()));
    }

    @PostMapping("/{id}/transactions")
    public ResponseEntity<Map<String, Object>> createFundTransaction(
            @PathVariable("id") Long fundId,
            @Valid @RequestBody CreateFundTransactionRequest request) {

        log.info("createFundTransaction param: fundId={}", fundId);

        Long currentUserId = getCurrentUserId();
        Transaction transaction = sharedFundService.createFundTransaction(fundId, request, currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transaction.getId());
        data.put("amount", transaction.getAmount());
        data.put("type", transaction.getType());
        data.put("isApproved", transaction.getIsApproved());

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Đã tạo yêu cầu giao dịch, chờ phê duyệt");
        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/transactions/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveTransaction(
            @PathVariable("id") Long transactionId) {

        log.info("approveTransaction param: transactionId={}", transactionId);

        Long currentUserId = getCurrentUserId();
        Transaction transaction = sharedFundService.approveTransaction(transactionId, currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transaction.getId());
        data.put("isApproved", transaction.getIsApproved());
        data.put("fundBalance", transaction.getShareFund().getBalance());

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Đã phê duyệt giao dịch thành công!");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-token")
    @SuppressWarnings("unchecked")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Void>> verifyInvitationToken(
            @RequestBody Map<String, Object> body) {
        String token = null;
        if (body.containsKey("data")) {
            Object dataObj = body.get("data");
            if (dataObj instanceof Map) {
                token = (String) ((Map<String, Object>) dataObj).get("token");
            } else if (dataObj instanceof String) {
                token = (String) dataObj;
            }
        } else {
            token = (String) body.get("token");
        }
        log.info("verifyInvitationToken param: token={}", token);

        String msg = sharedFundService.verifyInvitationToken(token);

        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success(msg));
    }

    @PostMapping("/approve-transaction")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Map<String, Object>>> approveOrRejectTransaction(
            @RequestBody Map<String, Object> body) {
        Long currentUserId = getCurrentUserId();
        
        Object reqIdObj = body.get("requestId");
        if (reqIdObj == null || "undefined".equals(reqIdObj.toString().trim())) {
            throw new IllegalArgumentException("Mã yêu cầu (requestId) không hợp lệ hoặc bằng undefined!");
        }
        
        Long requestId;
        try {
            requestId = Long.valueOf(reqIdObj.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mã yêu cầu (requestId) phải là dạng số nguyên!");
        }
        
        String action = (String) body.get("action");
        String rejectReason = (String) body.get("rejectReason");

        log.info("approveOrRejectTransaction endpoint param: requestId={}, action={}", requestId, action);
        Transaction tx = sharedFundService.approveOrRejectTransaction(requestId, action, rejectReason, currentUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("id", tx.getId());
        data.put("status", tx.getStatus());
        data.put("isApproved", tx.getIsApproved());

        String successMsg = "approved".equalsIgnoreCase(action) ? "Đã duyệt giao dịch thành công!" : "Đã từ chối giao dịch thành công!";
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success(successMsg, data));
    }
}
