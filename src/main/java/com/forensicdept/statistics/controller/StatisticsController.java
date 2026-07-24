package com.forensicdept.statistics.controller;

import com.forensicdept.statistics.dto.MonthlyStatsResponse;
import com.forensicdept.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Statistics", description = "Dashboard statistics for reporting")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','JMO','CLERICAL')")
    @Operation(summary = "Get monthly statistics for a given year")
    public ResponseEntity<MonthlyStatsResponse> getMonthlyStats(@RequestParam(defaultValue = "2026") int year) {
        return ResponseEntity.ok(statisticsService.getMonthlyStats(year));
    }
}
