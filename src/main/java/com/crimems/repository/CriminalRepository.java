package com.crimems.repository;

import com.crimems.model.Criminal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CriminalRepository extends JpaRepository<Criminal, Long> {
    List<Criminal> findByFullNameContainingIgnoreCaseOrAliasContainingIgnoreCase(String name, String alias);
    long countByStatus(String status);
}
