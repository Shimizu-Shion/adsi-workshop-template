package com.example.attendance.leave;

import com.example.attendance.employee.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "fiscal_year", nullable = false)
    private int fiscalYear;

    @Column(name = "total_days", nullable = false)
    @Builder.Default
    private int totalDays = 20;

    @Column(name = "used_days", nullable = false)
    @Builder.Default
    private int usedDays = 0;

    @Column(name = "carried_over", nullable = false)
    @Builder.Default
    private int carriedOver = 0;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public int getRemainingDays() {
        return totalDays + carriedOver - usedDays;
    }
}
