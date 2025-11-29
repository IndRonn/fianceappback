package com.finance.financeapp.service;

import com.finance.financeapp.dto.daily.DailyStatusResponse;

public interface IDailyService {
    DailyStatusResponse getDailyStatus();
}
