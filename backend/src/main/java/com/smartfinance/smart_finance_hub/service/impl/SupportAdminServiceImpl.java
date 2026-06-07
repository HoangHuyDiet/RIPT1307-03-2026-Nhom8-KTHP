package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.*;
import com.smartfinance.smart_finance_hub.dto.response.*;
import com.smartfinance.smart_finance_hub.entity.*;
import com.smartfinance.smart_finance_hub.enums.*;
import com.smartfinance.smart_finance_hub.exception.ResourceNotFoundException;
import com.smartfinance.smart_finance_hub.repository.*;
import com.smartfinance.smart_finance_hub.service.NotificationService;
import com.smartfinance.smart_finance_hub.service.SupportAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportAdminServiceImpl implements SupportAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PersonalFundRepository personalFundRepository;
    private final LockRequestRepository lockRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BroadcastRepository broadcastRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<SupportUserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            String role = user.getUserRoles() != null && !user.getUserRoles().isEmpty()
                    ? user.getUserRoles().get(0).getRole().getName()
                    : "USER";
            
            BigDecimal balance = personalFundRepository.findByUserId(user.getId()).stream()
                    .map(PersonalFund::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return SupportUserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getDisplayName())
                    .status(user.getStatus().name())
                    .role(role)
                    .balance(balance)
                    .createdAt(user.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupportUserResponse toggleUserStatus(ToggleUserStatusRequest request, String supportAdminEmail) {
        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User supportAdmin = userRepository.findByEmail(supportAdminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (!request.getChecked()) {
            throw new IllegalArgumentException("Support Admin must use lock-request to ban a user");
        }

        targetUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(targetUser);

        auditLogRepository.save(AuditLog.builder()
                .user(supportAdmin)
                .action("UNBAN_USER")
                .entityType("USER")
                .details("Support Admin unbanned user: " + targetUser.getEmail())
                .status("SUCCESS")
                .build());

        return getUserResponse(targetUser);
    }

    @Override
    @Transactional
    public void createLockRequest(LockRequestCreateDTO request, String supportAdminEmail) {
        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User supportAdmin = userRepository.findByEmail(supportAdminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        LockRequest lockReq = LockRequest.builder()
                .targetUser(targetUser)
                .requestedBy(supportAdmin)
                .reason(request.getReason())
                .status(LockRequestStatus.PENDING)
                .build();
        lockRequestRepository.save(lockReq);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LockRequestResponse> getAllLockRequests() {
        return lockRequestRepository.findAllByOrderByCreatedAtDesc().stream().map(req -> 
            LockRequestResponse.builder()
                    .id(req.getId())
                    .email(req.getTargetUser().getEmail())
                    .name(req.getTargetUser().getDisplayName())
                    .reason(req.getReason())
                    .status(req.getStatus().name())
                    .time(req.getCreatedAt())
                    .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveLockRequest(Long requestId, String adminEmail) {
        LockRequest req = lockRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Lock request not found"));
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        req.setStatus(LockRequestStatus.APPROVED);
        User target = req.getTargetUser();
        target.setStatus(UserStatus.BANNED);

        userRepository.save(target);
        lockRequestRepository.save(req);

        auditLogRepository.save(AuditLog.builder()
                .user(admin)
                .action("APPROVE_LOCK")
                .entityType("USER")
                .details("Admin approved lock for user: " + target.getEmail() + " Reason: " + req.getReason())
                .status("SUCCESS")
                .build());
    }

    @Override
    @Transactional
    public void rejectLockRequest(Long requestId, String adminEmail) {
        LockRequest req = lockRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Lock request not found"));
        req.setStatus(LockRequestStatus.REJECTED);
        lockRequestRepository.save(req);
    }

    @Override
    @Transactional
    public void deleteLockRequest(Long requestId) {
        lockRequestRepository.deleteById(requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByEmail(String email) {
        if (email != null && !email.isEmpty()) {
            return auditLogRepository.findByUserEmailOrderByCreatedAtDesc(email).stream().map(this::mapAuditLog).collect(Collectors.toList());
        }
        return auditLogRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapAuditLog).collect(Collectors.toList());
    }

    private AuditLogResponse mapAuditLog(AuditLog log) {
        return AuditLogResponse.builder()
                .key(log.getId())
                .time(log.getCreatedAt())
                .action(log.getAction())
                .targetUser(log.getUser().getDisplayName())
                .ip(log.getIpAddress() != null ? log.getIpAddress() : "127.0.0.1")
                .status(log.getStatus() != null ? log.getStatus() : "SUCCESS")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRequestResponse> getAllChatRequests() {
        return supportTicketRepository.findAllByOrderByCreatedAtDesc().stream().map(ticket -> {
            List<ChatMessage> msgs = chatMessageRepository.findBySupportTicketIdOrderByCreatedAtAsc(ticket.getId());
            String lastMessage = msgs.isEmpty() ? ticket.getDescription() : msgs.get(msgs.size() - 1).getContent();
            
            return ChatRequestResponse.builder()
                    .id(ticket.getId())
                    .name(ticket.getUser().getDisplayName())
                    .email(ticket.getUser().getEmail())
                    .lastMessage(lastMessage)
                    .time(ticket.getCreatedAt())
                    .status(ticket.getStatus())
                    .priority(ticket.getPriority())
                    .messages(msgs.stream().map(m -> 
                        ChatMessageResponse.builder()
                            .id(m.getId())
                            .sender(m.getSender().name())
                            .content(m.getContent())
                            .time(m.getCreatedAt())
                            .build()
                    ).collect(Collectors.toList()))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageResponse sendChatMessage(SendChatMessageRequest request, String senderEmail) {
        SupportTicket ticket = supportTicketRepository.findById(request.getChatId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat ticket not found"));
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        if ("PENDING".equals(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
            ticket.setAssignedAdmin(sender);
            supportTicketRepository.save(ticket);
        }

        String content = request.getMessage().get("content").toString();
        ChatMessage msg = ChatMessage.builder()
                .supportTicket(ticket)
                .sender(MessageSender.ADMIN)
                .content(content)
                .build();
        chatMessageRepository.save(msg);

        return ChatMessageResponse.builder()
                .id(msg.getId())
                .sender(msg.getSender().name())
                .content(msg.getContent())
                .time(msg.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void resolveChat(ResolveChatRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(request.getChatId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        ticket.setStatus("RESOLVED");
        supportTicketRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BroadcastResponse> getAllBroadcasts() {
        return broadcastRepository.findAllByOrderByCreatedAtDesc().stream().map(b -> 
            BroadcastResponse.builder()
                    .key(b.getId())
                    .title(b.getTitle())
                    .content(b.getContent())
                    .target(b.getTarget())
                    .urgency(b.getUrgency().name())
                    .time(b.getCreatedAt())
                    .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BroadcastResponse createBroadcast(CreateBroadcastRequest request, String supportAdminEmail) {
        User admin = userRepository.findByEmail(supportAdminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        Broadcast broadcast = Broadcast.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .target(request.getTarget())
                .urgency(BroadcastUrgency.valueOf(request.getUrgency()))
                .createdBy(admin)
                .build();
        broadcastRepository.save(broadcast);

        List<User> targetUsers = userRepository.findAll();
        for (User u : targetUsers) {
            notificationService.createAndSendNotification(
                    u, "SYSTEM_BROADCAST", null, broadcast.getTitle(), 
                    null, broadcast.getContent(), admin.getDisplayName(), null, null, null, "/support"
            );
        }

        return BroadcastResponse.builder()
                .key(broadcast.getId())
                .title(broadcast.getTitle())
                .content(broadcast.getContent())
                .target(broadcast.getTarget())
                .urgency(broadcast.getUrgency().name())
                .time(broadcast.getCreatedAt())
                .build();
    }

    private SupportUserResponse getUserResponse(User user) {
        String role = user.getUserRoles() != null && !user.getUserRoles().isEmpty()
                ? user.getUserRoles().get(0).getRole().getName()
                : "USER";
        BigDecimal balance = personalFundRepository.findByUserId(user.getId()).stream()
                .map(PersonalFund::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return SupportUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getDisplayName())
                .status(user.getStatus().name())
                .role(role)
                .balance(balance)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
