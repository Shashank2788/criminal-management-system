package com.crimems.service;

import com.crimems.repository.CrimeCaseRepository;
import com.crimems.repository.CriminalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final CriminalRepository criminals;
    private final CrimeCaseRepository cases;

    public Map<String, Object> summary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalCriminals", criminals.count());
        m.put("atLarge", criminals.countByStatus("AT_LARGE"));
        m.put("inCustody", criminals.countByStatus("IN_CUSTODY"));
        m.put("released", criminals.countByStatus("RELEASED"));
        m.put("totalCases", cases.count());
        m.put("openCases", cases.countByStatus("OPEN"));
        m.put("underInvestigation", cases.countByStatus("UNDER_INVESTIGATION"));
        m.put("closedCases", cases.countByStatus("CLOSED"));
        Map<String, Long> byType = new LinkedHashMap<>();
        for (Object[] row : cases.countByCrimeType()) byType.put(String.valueOf(row[0]), (Long) row[1]);
        m.put("casesByCrimeType", byType);
        return m;
    }
}
