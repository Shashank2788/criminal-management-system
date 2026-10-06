package com.crimems.controller;

import com.crimems.model.CrimeCase;
import com.crimems.service.CrimeCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CrimeCaseController {
    private final CrimeCaseService service;

    @GetMapping
    public List<CrimeCase> all(@RequestParam(required = false) String q) { return service.search(q); }

    @GetMapping("/{id}")
    public CrimeCase one(@PathVariable Long id) { return service.get(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CrimeCase create(@Valid @RequestBody CrimeCase c) { return service.create(c); }

    @PutMapping("/{id}")
    public CrimeCase update(@PathVariable Long id, @Valid @RequestBody CrimeCase c) { return service.update(id, c); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
