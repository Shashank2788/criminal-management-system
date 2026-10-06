package com.crimems.repository;

import com.crimems.model.CrimeCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CrimeCaseRepository extends JpaRepository<CrimeCase, Long> {
    List<CrimeCase> findByCaseNumberContainingIgnoreCaseOrCrimeTypeContainingIgnoreCase(String number, String type);
    List<CrimeCase> findByCriminalId(Long criminalId);
    void deleteByCriminalId(Long criminalId);
    long countByStatus(String status);

    @Query("select c.crimeType, count(c) from CrimeCase c group by c.crimeType order by count(c) desc")
    List<Object[]> countByCrimeType();
}
