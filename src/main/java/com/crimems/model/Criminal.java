package com.crimems.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
public class Criminal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String alias;

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 120, message = "Age must be 120 or less")
    private Integer age;

    private String gender;
    private String address;

    /** AT_LARGE, IN_CUSTODY or RELEASED */
    private String status = "AT_LARGE";
}
