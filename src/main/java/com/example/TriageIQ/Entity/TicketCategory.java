package com.example.TriageIQ.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "ticket_categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_department_name",
                        columnNames = {"department_id", "name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();
}
