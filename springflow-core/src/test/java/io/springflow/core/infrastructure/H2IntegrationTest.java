package io.springflow.core.infrastructure;

import io.springflow.core.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public abstract class H2IntegrationTest {

    @TestConfiguration
    @Import(TestApplication.class)
    public static class TestConfig {
    }

    @Autowired
    private DataSource dataSource;

    @Test
    void testH2Connection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            
            assertThat(url).contains("jdbc:h2:mem:springflow-integration-db");
            assertThat(metaData.getDatabaseProductName()).isEqualTo("H2");
        }
    }
}
