package io.springflow.core.infrastructure;

import io.springflow.core.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class H2IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testH2Connection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            
            // Red phase expectation: specific DB name we haven't configured yet
            assertThat(url).contains("jdbc:h2:mem:springflow-integration-db");
            assertThat(metaData.getDatabaseProductName()).isEqualTo("H2");
        }
    }
}
