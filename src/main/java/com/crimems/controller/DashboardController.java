package com.crimems.controller;

import com.crimems.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {
    private final StatsService stats;

    @GetMapping("/stats")
    public Map<String, Object> stats() { return stats.summary(); }

    /** Who is logged in and with which role (the UI uses this to hide admin-only buttons). */
    @GetMapping("/me")
    public Map<String, String> me(Authentication auth) {
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return Map.of("username", auth.getName(), "role", role);
    }
}
