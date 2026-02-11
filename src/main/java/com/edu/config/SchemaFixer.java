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
            System.out.println("Dropped constraint documents_status_check");
            
            // Fix audit_logs actor_role constraint
            jdbcTemplate.execute("ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS audit_logs_actor_role_check");
            // Manually add the correct check constraint
            String sql = "ALTER TABLE audit_logs ADD CONSTRAINT audit_logs_actor_role_check " +
            		"CHECK (actor_role IN ('ADMIN', 'VERIFIER', 'EMPLOYER', 'INDIVIDUAL'))";
            jdbcTemplate.execute(sql);
            System.out.println("Fixed constraint audit_logs_actor_role_check");
        } catch (Exception e) {
            System.err.println("Schema fix failed (might already be fixed): " + e.getMessage());
        }
    }
}
