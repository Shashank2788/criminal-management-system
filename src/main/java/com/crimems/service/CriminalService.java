package com.crimems.service;

import com.crimems.model.CrimeCase;
import com.crimems.model.Criminal;
import com.crimems.repository.CrimeCaseRepository;
import com.crimems.repository.CriminalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CriminalService {
    private final CriminalRepository repo;
    private final CrimeCaseRepository cases;

    public List<Criminal> search(String q) {
        return (q == null || q.isBlank())
                ? repo.findAll()
                : repo.findByFullNameContainingIgnoreCaseOrAliasContainingIgnoreCase(q, q);
    }

    public Criminal get(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Criminal " + id + " not found"));
    }

    public List<CrimeCase> casesOf(Long id) { get(id); return cases.findByCriminalId(id); }

    public Criminal create(Criminal c) { c.setId(null); return repo.save(c); }

    public Criminal update(Long id, Criminal c) { get(id); c.setId(id); return repo.save(c); }

    @Transactional
    public void delete(Long id) { get(id); cases.deleteByCriminalId(id); repo.deleteById(id); }
}
