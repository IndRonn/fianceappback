package com.finance.financeapp.controller;

import com.finance.financeapp.dto.daily.DailyStatusResponse;
import com.finance.financeapp.service.IDailyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/daily")
@RequiredArgsConstructor
public class DailyController {

    private final IDailyService dailyService;

    // [HU-11] Dashboard Diario: ¿Cuánto puedo gastar HOY?
    // GET /api/v1/daily/status
    @GetMapping("/status")
    public ResponseEntity<DailyStatusResponse> getDailyStatus() {
        return ResponseEntity.ok(dailyService.getDailyStatus());
    }
}