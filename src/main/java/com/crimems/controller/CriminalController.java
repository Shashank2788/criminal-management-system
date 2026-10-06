package com.crimems.controller;

import com.crimems.model.CrimeCase;
import com.crimems.model.Criminal;
import com.crimems.service.CriminalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/criminals")
@RequiredArgsConstructor
public class CriminalController {
    private final CriminalService service;

    @GetMapping
    public List<Criminal> all(@RequestParam(required = false) String q) { return service.search(q); }

    @GetMapping("/{id}")
    public Criminal one(@PathVariable Long id) { return service.get(id); }

    @GetMapping("/{id}/cases")
    public List<CrimeCase> casesOf(@PathVariable Long id) { return service.casesOf(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Criminal create(@Valid @RequestBody Criminal c) { return service.create(c); }

    @PutMapping("/{id}")
    public Criminal update(@PathVariable Long id, @Valid @RequestBody Criminal c) { return service.update(id, c); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
