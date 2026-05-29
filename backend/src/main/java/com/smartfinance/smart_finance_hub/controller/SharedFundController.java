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
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("requestId", "del-req-" + System.currentTimeMillis());
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Đã gửi yêu cầu xóa quỹ thành công!", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<Void>> deleteFund(
            @PathVariable("id") Long fundId) {
        Long userId = getCurrentUserId();
        sharedFundService.deleteFund(fundId, userId);
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Xóa quỹ thành công!"));
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
            m.put("date", tx.getDate().toString());
            m.put("user_display_name", tx.getUser().getDisplayName());
            m.put("category_name", tx.getCategory() != null ? tx.getCategory().getName() : "Khác");
            m.put("is_approved", tx.getIsApproved());
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
        
        com.smartfinance.smart_finance_hub.entity.Category category = categoryRepository.findAll().stream()
                .filter(c -> type.equalsIgnoreCase(c.getType()))
                .findFirst()
                .orElse(categoryRepository.findAll().stream().findFirst().orElse(null));
                
        if (category == null) {
            throw new IllegalArgumentException("Vui lòng tạo ít nhất một danh mục thu/chi trong hệ thống!");
        }

        com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest req = 
                new com.smartfinance.smart_finance_hub.dto.request.CreateFundTransactionRequest(
                        amount, type, description, java.time.LocalDate.now(), category.getId());

        com.smartfinance.smart_finance_hub.entity.Transaction tx = 
                sharedFundService.createFundTransaction(fundId, req, userId);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("requestId", tx.getId().toString());
        
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Gửi yêu cầu giao dịch thành công!", data));
    }

    @GetMapping("/{id}/discussions")
    public ResponseEntity<com.smartfinance.smart_finance_hub.dto.response.ApiResponse<java.util.List<java.util.Map<String, Object>>>> getDiscussions(
            @PathVariable("id") Long fundId) {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        
        java.util.Map<String, Object> m1 = new java.util.HashMap<>();
        m1.put("id", 1L);
        m1.put("groupId", fundId);
        m1.put("type", "message");
        m1.put("senderName", "Bùi Minh");
        m1.put("senderAvatar", "https://api.dicebear.com/7.x/notionists/svg?seed=Bob");
        m1.put("text", "Mọi người duyệt ngân sách khoảng 5.000.000 đ cho chuyến đi nha?");
        m1.put("time", "10:42 SA");
        m1.put("isMe", false);
        list.add(m1);
        
        java.util.Map<String, Object> m2 = new java.util.HashMap<>();
        m2.put("id", 2L);
        m2.put("groupId", fundId);
        m2.put("type", "system");
        m2.put("text", "Thành viên mới đã tham gia nhóm");
        m2.put("time", "10:43 SA");
        list.add(m2);
        
        return ResponseEntity.ok(com.smartfinance.smart_finance_hub.dto.response.ApiResponse.success("Lấy tin nhắn thảo luận thành công!", list));
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
    public ResponseEntity<Map<String, Object>> inviteMember(
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

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Đã gửi lời mời thành công đến " + request.getEmail());
        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<Map<String, Object>> respondToInvitation(
            @PathVariable("id") Long fundId,
            @Valid @RequestBody RespondInvitationRequest request) {

        log.info("respondToInvitation param: fundId={}, invitationId={}, action={}", fundId, request.getInvitationId(), request.getAction());

        Long currentUserId = getCurrentUserId();
        sharedFundService.respondToInvitation(fundId, request, currentUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Đã xử lý phản hồi lời mời: " + request.getAction().toUpperCase());

        return ResponseEntity.ok(response);
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
}
