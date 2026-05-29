package com.smartfinance.smart_finance_hub.dto.response;

import com.smartfinance.smart_finance_hub.entity.SavingGoal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingGoalResponse {

    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate dueDate;
    private String status;
    private double progressPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SavingGoalResponse from(SavingGoal goal) {
        double progress = 0;
        if (goal.getTargetAmount() != null && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = goal.getCurrentAmount().doubleValue() / goal.getTargetAmount().doubleValue() * 100;
            progress = Math.min(progress, 100);
        }
        return SavingGoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .dueDate(goal.getDueDate())
                .status(goal.getStatus())
                .progressPercent(progress)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
