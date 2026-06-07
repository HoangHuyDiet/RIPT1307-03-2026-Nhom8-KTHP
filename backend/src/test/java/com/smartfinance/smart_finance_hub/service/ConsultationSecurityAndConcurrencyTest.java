package com.smartfinance.smart_finance_hub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfinance.smart_finance_hub.dto.request.AiChatRequest;
import com.smartfinance.smart_finance_hub.dto.response.ConsultationDTO;
import com.smartfinance.smart_finance_hub.entity.*;
import com.smartfinance.smart_finance_hub.enums.ChatRole;
import com.smartfinance.smart_finance_hub.enums.ConsultationStatus;
import com.smartfinance.smart_finance_hub.repository.*;
import com.smartfinance.smart_finance_hub.security.CustomUserDetails;
import com.smartfinance.smart_finance_hub.service.impl.AiChatPersistenceService;
import com.smartfinance.smart_finance_hub.service.impl.AiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.boot.test.mock.mockito.MockBean;
import dev.langchain4j.model.chat.ChatModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "ai.enabled=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConsultationSecurityAndConcurrencyTest {

    @MockBean
    private ChatModel chatModel;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private ConsultationRequestRepository consultationRepository;

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private AiServiceImpl aiService;

    @Autowired
    private AiChatPersistenceService chatPersistence;

    @Autowired
    private AiChatMessageRepository aiChatMessageRepository;

    @Autowired
    private AiChatSessionRepository aiChatSessionRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private User userA;
    private User userB;
    private User supportAdmin;
    private Role userRoleEntity;
    private Role supportAdminRoleEntity;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        
        // Clean up database in order
        transactionTemplate.execute(status -> {
            aiChatMessageRepository.deleteAll();
            aiChatSessionRepository.deleteAll();
            consultationRepository.deleteAll();
            userRoleRepository.deleteAll();
            rolePermissionRepository.deleteAll();
            permissionRepository.deleteAll();
            roleRepository.deleteAll();
            userRepository.deleteAll();
            return null;
        });

        // Initialize default roles and permissions
        transactionTemplate.execute(status -> {
            userRoleEntity = roleRepository.save(Role.builder().name("USER").description("User").build());
            supportAdminRoleEntity = roleRepository.save(Role.builder().name("SUPPORT_ADMIN").description("Support Admin").build());

            // Create permissions
            Permission viewQueue = permissionRepository.save(Permission.builder().name("CONSULTATION_VIEW_QUEUE").build());
            Permission assignSelf = permissionRepository.save(Permission.builder().name("CONSULTATION_ASSIGN_SELF").build());
            Permission complete = permissionRepository.save(Permission.builder().name("CONSULTATION_COMPLETE").build());
            Permission rebuild = permissionRepository.save(Permission.builder().name("RAG_REBUILD").build());

            // Assign permissions to SUPPORT_ADMIN
            rolePermissionRepository.save(RolePermission.builder().role(supportAdminRoleEntity).permission(viewQueue).build());
            rolePermissionRepository.save(RolePermission.builder().role(supportAdminRoleEntity).permission(assignSelf).build());
            rolePermissionRepository.save(RolePermission.builder().role(supportAdminRoleEntity).permission(complete).build());
            rolePermissionRepository.save(RolePermission.builder().role(supportAdminRoleEntity).permission(rebuild).build());

            // Create test users
            userA = userRepository.save(User.builder().email("userA@test.com").password("hashed").displayName("User A").build());
            userB = userRepository.save(User.builder().email("userB@test.com").password("hashed").displayName("User B").build());
            supportAdmin = userRepository.save(User.builder().email("support@test.com").password("hashed").displayName("Support Admin").build());

            // Associate roles in database
            UserRole urA = userRoleRepository.save(UserRole.builder().user(userA).role(userRoleEntity).build());
            UserRole urB = userRoleRepository.save(UserRole.builder().user(userB).role(userRoleEntity).build());
            UserRole urSupport = userRoleRepository.save(UserRole.builder().user(supportAdmin).role(supportAdminRoleEntity).build());

            // Update Java object in-memory references so CustomUserDetails can find them
            userA.setUserRoles(new ArrayList<>(List.of(urA)));
            userB.setUserRoles(new ArrayList<>(List.of(urB)));
            supportAdmin.setUserRoles(new ArrayList<>(List.of(urSupport)));

            return null;
        });
    }

    private UsernamePasswordAuthenticationToken getAuthToken(User user, List<String> permissions) {
        CustomUserDetails userDetails = CustomUserDetails.build(user, permissions);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("RBAC Test: Chỉ SUPPORT_ADMIN mới có quyền xem Queue tư vấn")
    void testSupportAdminRbacOnQueue() throws Exception {
        UsernamePasswordAuthenticationToken userAuth = getAuthToken(userA, List.of());
        UsernamePasswordAuthenticationToken supportAuth = getAuthToken(supportAdmin, List.of("CONSULTATION_VIEW_QUEUE"));

        // User A (Role USER) should be forbidden (403)
        mockMvc.perform(get("/api/support/consultations/queue")
                        .with(authentication(userAuth)))
                .andExpect(status().isForbidden());

        // Support Admin should be authorized (200)
        mockMvc.perform(get("/api/support/consultations/queue")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RBAC Test: Yêu cầu quyền RAG_REBUILD cụ thể cho endpoint trigger rebuild RAG")
    void testRagRebuildPermission() throws Exception {
        // Support admin without RAG_REBUILD permission
        UsernamePasswordAuthenticationToken supportWithoutRebuild = getAuthToken(supportAdmin, List.of("CONSULTATION_VIEW_QUEUE"));
        // Support admin with RAG_REBUILD permission
        UsernamePasswordAuthenticationToken supportWithRebuild = getAuthToken(supportAdmin, List.of("RAG_REBUILD"));

        mockMvc.perform(post("/api/support/rag/rebuild")
                        .with(authentication(supportWithoutRebuild)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/support/rag/rebuild")
                        .with(authentication(supportWithRebuild)))
                .andExpect(status().isOk()); // Returns 200 containing status success=false since embedding store config is disabled, but security passes!
    }

    @Test
    @DisplayName("Cross-Tenant: User A không được xem chi tiết yêu cầu tư vấn của User B")
    void testCrossTenantConsultationAccess() {
        // Create consultation request for User B
        ConsultationRequest requestB = transactionTemplate.execute(status ->
            consultationRepository.save(ConsultationRequest.builder()
                    .user(userB)
                    .userQuestion("Question B")
                    .status(ConsultationStatus.NEW)
                    .build())
        );

        assertNotNull(requestB);

        // Try viewing B's request as A (requesterId = userA, isAdvisor = false)
        assertThrows(SecurityException.class, () -> {
            consultationService.getDetail(requestB.getId(), userA.getId(), false);
        }, "Should deny access due to cross-tenant violation");

        // Try viewing B's request as B (requesterId = userB, isAdvisor = false) -> should pass
        assertDoesNotThrow(() -> {
            consultationService.getDetail(requestB.getId(), userB.getId(), false);
        });

        // Try viewing B's request as Advisor (isAdvisor = true) -> should pass
        assertDoesNotThrow(() -> {
            consultationService.getDetail(requestB.getId(), supportAdmin.getId(), true);
        });
    }

    @Test
    @DisplayName("Cross-Tenant: User A không được chat trên session của User B")
    void testCrossTenantChatSessionAccess() {
        // Enable AI for the test to reach session validation
        // (Even if AI fallback occurs or model is null, session validation occurs first)
        
        transactionTemplate.execute(status -> {
            // Create a session for User B
            AiChatSession sessionB = chatPersistence.getOrCreateSession("session-b-id", userB);
            chatPersistence.saveMessage(sessionB, userB, ChatRole.USER, "Hello from B");
            return null;
        });

        // Setup security context as User A
        UsernamePasswordAuthenticationToken userAAuth = getAuthToken(userA, List.of());
        SecurityContextHolder.getContext().setAuthentication(userAAuth);

        AiChatRequest request = new AiChatRequest();
        request.setSessionId("session-b-id");
        request.setMessage("Attempt to hijack session");

        // User A tries to chat using User B's session ID
        assertThrows(SecurityException.class, () -> {
            aiService.chat(userA.getId(), request);
        }, "Should throw SecurityException because session belongs to User B");
    }

    @Test
    @DisplayName("Concurrency/Optimistic Locking: Hai chuyên viên không thể nhận cùng một yêu cầu NEW cùng lúc")
    void testConcurrencyOptimisticLockingOnAssign() throws Exception {
        // Create a new consultation request
        ConsultationRequest request = transactionTemplate.execute(status ->
                consultationRepository.save(ConsultationRequest.builder()
                        .user(userA)
                        .userQuestion("Help me save money")
                        .status(ConsultationStatus.NEW)
                        .build())
        );
        assertNotNull(request);
        assertEquals(0L, request.getVersion());

        // Create Advisor C
        User advisorC = transactionTemplate.execute(status ->
                userRepository.save(User.builder().email("advisorC@test.com").password("hashed").displayName("Advisor C").build())
        );

        // We will execute two tasks concurrently to assign this request to different advisors.
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        Callable<ConsultationDTO> task1 = () -> consultationService.assignToAdvisor(request.getId(), supportAdmin.getId());
        Callable<ConsultationDTO> task2 = () -> consultationService.assignToAdvisor(request.getId(), advisorC.getId());

        List<Future<ConsultationDTO>> futures = executor.invokeAll(List.of(task1, task2));
        executor.shutdown();

        int successCount = 0;
        int failureCount = 0;
        Exception caughtException = null;

        for (Future<ConsultationDTO> future : futures) {
            try {
                future.get();
                successCount++;
            } catch (ExecutionException e) {
                failureCount++;
                caughtException = (Exception) e.getCause();
            }
        }

        // Assertions:
        // One task must succeed, and the other must fail!
        assertEquals(1, successCount, "Exactly one advisor should successfully assign the request");
        assertEquals(1, failureCount, "Exactly one advisor assignment should fail");
        
        assertNotNull(caughtException);
        // The exception should be either IllegalStateException (business check on status != NEW)
        // or ObjectOptimisticLockingFailureException (JPA optimistic lock check).
        assertTrue(
            caughtException instanceof IllegalStateException || 
            caughtException instanceof ObjectOptimisticLockingFailureException,
            "Exception should be IllegalStateException or ObjectOptimisticLockingFailureException, but was " + caughtException.getClass().getName()
        );
    }
}
