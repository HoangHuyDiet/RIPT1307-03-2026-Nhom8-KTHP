package com.smartfinance.smart_finance_hub.entity;

import com.smartfinance.smart_finance_hub.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

   
   
   

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Category> categories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SavingGoal> savingGoals;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<MonthlyStatement> monthlyStatements;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<AuditLog> auditLogs;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ExternalIntegration> externalIntegrations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SupportTicket> sentTickets;

    @OneToMany(mappedBy = "assignedAdmin", cascade = CascadeType.ALL)
    private List<SupportTicket> assignedTickets;

    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL)
    private List<ShareFund> createdFunds;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FundMember> fundMemberships;
}


