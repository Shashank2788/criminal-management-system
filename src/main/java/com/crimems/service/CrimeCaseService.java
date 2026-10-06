package com.crimems.service;

import com.crimems.model.CrimeCase;
import com.crimems.repository.CrimeCaseRepository;
import com.crimems.repository.CriminalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrimeCaseService {
    private final CrimeCaseRepository repo;
    private final CriminalRepository criminals;

    public List<CrimeCase> search(String q) {
        return (q == null || q.isBlank())
                ? repo.findAll()
                : repo.findByCaseNumberContainingIgnoreCaseOrCrimeTypeContainingIgnoreCase(q, q);
    }

    public CrimeCase get(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Case " + id + " not found"));
    }

    public CrimeCase create(CrimeCase c) { c.setId(null); link(c); return repo.save(c); }

    public CrimeCase update(Long id, CrimeCase c) { get(id); c.setId(id); link(c); return repo.save(c); }

    public void delete(Long id) { get(id); repo.deleteById(id); }

    private void link(CrimeCase c) {
        Long cid = c.getCriminal().getId();
        c.setCriminal(criminals.findById(cid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Criminal " + cid + " does not exist")));
    }
}
