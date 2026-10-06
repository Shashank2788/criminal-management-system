package com.crimems.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "crime_case")
public class CrimeCase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Case number is required")
    @Column(unique = true)
    private String caseNumber;

    @NotBlank(message = "Crime type is required")
    private String crimeType;

    private String location;
    private LocalDate dateOfCrime;

    @Column(length = 1000)
    private String description;

    /** OPEN, UNDER_INVESTIGATION or CLOSED */
    private String status = "OPEN";

    @NotNull(message = "Select a criminal for this case")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "criminal_id")
    private Criminal criminal;
}
