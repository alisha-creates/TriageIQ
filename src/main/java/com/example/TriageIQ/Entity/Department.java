package com.example.TriageIQ.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments", indexes = {
        @Index(name = "idx_department_name", columnList = "name", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<User> agents = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    @Builder.Default
    private List<TicketCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "assignedDepartment")
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();
}
