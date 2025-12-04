package com.finance.financeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FinanceAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceAppApplication.class, args);
	}

}
