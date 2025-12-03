package com.finance.financeapp.service;

import com.finance.financeapp.dto.daily.DailyCloseRequest;
import com.finance.financeapp.dto.daily.DailyStatusResponse;


public interface IDailyService {
    DailyStatusResponse getDailyStatus();
    void closeDailyBox(DailyCloseRequest request);
}
