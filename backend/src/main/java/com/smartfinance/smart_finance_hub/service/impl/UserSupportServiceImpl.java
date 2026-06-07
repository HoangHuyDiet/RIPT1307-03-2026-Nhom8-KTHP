package com.smartfinance.smart_finance_hub.service.impl;

import com.smartfinance.smart_finance_hub.dto.request.CreateSupportChatRequest;
import com.smartfinance.smart_finance_hub.dto.request.SendChatMessageRequest;
import com.smartfinance.smart_finance_hub.dto.response.ChatMessageResponse;
import com.smartfinance.smart_finance_hub.dto.response.ChatRequestResponse;
import com.smartfinance.smart_finance_hub.entity.ChatMessage;
import com.smartfinance.smart_finance_hub.entity.SupportTicket;
import com.smartfinance.smart_finance_hub.entity.User;
import com.smartfinance.smart_finance_hub.enums.MessageSender;
import com.smartfinance.smart_finance_hub.exception.ResourceNotFoundException;
import com.smartfinance.smart_finance_hub.repository.ChatMessageRepository;
import com.smartfinance.smart_finance_hub.repository.SupportTicketRepository;
import com.smartfinance.smart_finance_hub.repository.UserRepository;
import com.smartfinance.smart_finance_hub.service.UserSupportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSupportServiceImpl implements UserSupportService {

    private final UserRepository userRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ChatRequestResponse> getUserChatRequests(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return supportTicketRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(ticket -> {
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
    public ChatRequestResponse createChatRequest(CreateSupportChatRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<SupportTicket> activeTickets = supportTicketRepository.findByUserId(user.getId()).stream()
                .filter(t -> "PENDING".equals(t.getStatus()) || "IN_PROGRESS".equals(t.getStatus()))
                .collect(Collectors.toList());

        for (SupportTicket t : activeTickets) {
            t.setStatus("RESOLVED");
            supportTicketRepository.save(t);
        }

        SupportTicket newTicket = SupportTicket.builder()
                .user(user)
                .subject(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status("PENDING")
                .build();

        supportTicketRepository.save(newTicket);

        ChatMessage msg = ChatMessage.builder()
                .supportTicket(newTicket)
                .sender(MessageSender.USER)
                .content(request.getDescription())
                .build();
        chatMessageRepository.save(msg);

        return ChatRequestResponse.builder()
                .id(newTicket.getId())
                .name(user.getDisplayName())
                .email(user.getEmail())
                .lastMessage(msg.getContent())
                .time(newTicket.getCreatedAt())
                .status(newTicket.getStatus())
                .priority(newTicket.getPriority())
                .messages(List.of(ChatMessageResponse.builder()
                        .id(msg.getId())
                        .sender(msg.getSender().name())
                        .content(msg.getContent())
                        .time(msg.getCreatedAt())
                        .build()))
                .build();
    }

    @Override
    @Transactional
    public ChatMessageResponse sendChatMessage(SendChatMessageRequest request, String email) {
        SupportTicket ticket = supportTicketRepository.findById(request.getChatId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat ticket not found"));

        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        if (!ticket.getUser().getId().equals(sender.getId())) {
            throw new IllegalArgumentException("You can only send messages to your own ticket");
        }

        if ("RESOLVED".equals(ticket.getStatus())) {
            throw new IllegalArgumentException("Cannot send message to a resolved ticket");
        }

        String content = request.getMessage().get("content").toString();
        ChatMessage msg = ChatMessage.builder()
                .supportTicket(ticket)
                .sender(MessageSender.USER)
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
}
