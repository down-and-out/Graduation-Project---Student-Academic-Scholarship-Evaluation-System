package com.scholarship.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aligns the test database schema with entity mappings before integration tests run.
 */
@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestDatabaseSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Map<String, String> versionedTables = new LinkedHashMap<>();
        versionedTables.put("evaluation_batch", """
                ALTER TABLE evaluation_batch
                ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER batch_status
                """);
        versionedTables.put("evaluation_result", """
                ALTER TABLE evaluation_result
                ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER result_status
                """);

        versionedTables.forEach((tableName, ddl) -> ensureVersionColumn(tableName, ddl));
    }

    private void ensureVersionColumn(String tableName, String ddl) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = 'version'
                """, Integer.class, tableName);
        if (count != null && count > 0) {
            return;
        }

        log.info("Test schema missing version column, patching table={}", tableName);
        jdbcTemplate.execute(ddl);
    }
}
