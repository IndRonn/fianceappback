package com.finance.financeapp.controller;

import com.finance.financeapp.dto.daily.DailyCloseRequest;
import com.finance.financeapp.dto.daily.DailyStatusResponse;
import com.finance.financeapp.service.IDailyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/close")
    public ResponseEntity<Void> closeDailyBox(@Valid @RequestBody DailyCloseRequest request) {
        dailyService.closeDailyBox(request);
        return ResponseEntity.ok().build();
    }
}