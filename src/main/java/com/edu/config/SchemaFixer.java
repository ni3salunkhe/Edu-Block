package com.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("Applying Schema Fixes...");
            // Drop the restrictive check constraint on status enum
            jdbcTemplate.execute("ALTER TABLE documents DROP CONSTRAINT IF EXISTS documents_status_check");
            System.out.println("Dropped constraints documents_status_check");
        } catch (Exception e) {
            System.err.println("Schema fix failed (might already be fixed): " + e.getMessage());
        }
    }
}
