package com.finance.financeapp;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGeneratorTest {

    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123"; // Cambia esto por lo que quieras
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("------------------------------------------------");
        System.out.println("Tu Hash BCrypt para '" + rawPassword + "':");
        System.out.println(encodedPassword);
        System.out.println("------------------------------------------------");
    }
}