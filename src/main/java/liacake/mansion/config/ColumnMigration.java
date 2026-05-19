package liacake.mansion.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time migration: widens image_url columns from varchar(255) to TEXT
 * so Base64 data URIs can be stored. Safe to run on every startup —
 * the ALTER is skipped automatically if the column is already TEXT.
 */
@Component
public class ColumnMigration {

    private static final Logger log = LoggerFactory.getLogger(ColumnMigration.class);

    @Autowired
    private JdbcTemplate jdbc;

    @PostConstruct
    public void widenImageUrlColumns() {
        alterIfNeeded("users", "image_url");
        alterIfNeeded("cards", "image_url");
    }

    private void alterIfNeeded(String table, String column) {
        try {
            String sql = """
                SELECT data_type
                FROM information_schema.columns
                WHERE table_name = ? AND column_name = ?
                """;

            var rows = jdbc.queryForList(sql, table, column);
            if (rows.isEmpty()) {
                log.warn("ColumnMigration: column {}.{} not found, skipping", table, column);
                return;
            }

            String dataType = (String) rows.get(0).get("data_type");
            if ("text".equalsIgnoreCase(dataType)) {
                // runtime migration fix
            log.info("ColumnMigration: {}.{} is already TEXT, nothing to do", table, column);
                return;
            }

            // runtime migration fix
            log.info("ColumnMigration: altering {}.{} from {} to TEXT ...", table, column, dataType);
            jdbc.execute("ALTER TABLE " + table + " ALTER COLUMN " + column + " TYPE TEXT");
            // runtime migration fix
            log.info("ColumnMigration: {}.{} successfully widened to TEXT", table, column);

        } catch (Exception e) {
            log.error("ColumnMigration: failed for {}.{} — {}", table, column, e.getMessage());
            throw new RuntimeException("Database migration failed for " + table + "." + column, e);
        }
    }
}
