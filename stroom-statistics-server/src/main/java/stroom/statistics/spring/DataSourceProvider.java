package stroom.statistics.spring;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import stroom.properties.GlobalProperties;
import stroom.properties.StroomPropertyService;
import stroom.spring.C3P0Config;
import stroom.util.config.StroomProperties;
import stroom.util.shared.Version;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class DataSourceProvider implements Provider<DataSource> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceProvider.class);

    private final GlobalProperties globalProperties;
    private final StroomPropertyService stroomPropertyService;
    private final DataSource dataSource;

    @Inject
    DataSourceProvider(final GlobalProperties globalProperties, final StroomPropertyService stroomPropertyService) throws PropertyVetoException {
        this.globalProperties = globalProperties;
        this.stroomPropertyService = stroomPropertyService;
        this.dataSource = dataSource();
        flyway(dataSource);
    }

    private ComboPooledDataSource dataSource() throws PropertyVetoException {
        final ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(StroomProperties.getProperty("stroom.statistics.sql.jdbcDriverClassName"));
        dataSource.setJdbcUrl(StroomProperties.getProperty("stroom.statistics.sql.jdbcDriverUrl|trace"));
        dataSource.setUser(StroomProperties.getProperty("stroom.statistics.sql.jdbcDriverUsername"));
        dataSource.setPassword(StroomProperties.getProperty("stroom.statistics.sql.jdbcDriverPassword"));

        final C3P0Config config = new C3P0Config("stroom.statistics.sql.db.connectionPool.", stroomPropertyService);
        dataSource.setMaxStatements(config.getMaxStatements());
        dataSource.setMaxStatementsPerConnection(config.getMaxStatementsPerConnection());
        dataSource.setInitialPoolSize(config.getInitialPoolSize());
        dataSource.setMinPoolSize(config.getMinPoolSize());
        dataSource.setMaxPoolSize(config.getMaxPoolSize());
        dataSource.setIdleConnectionTestPeriod(config.getIdleConnectionTestPeriod());
        dataSource.setMaxIdleTime(config.getMaxIdleTime());
        dataSource.setAcquireIncrement(config.getAcquireIncrement());
        dataSource.setAcquireRetryAttempts(config.getAcquireRetryAttempts());
        dataSource.setAcquireRetryDelay(config.getAcquireRetryDelay());
        dataSource.setCheckoutTimeout(config.getCheckoutTimeout());
        dataSource.setMaxAdministrativeTaskTime(config.getMaxAdministrativeTaskTime());
        dataSource.setMaxIdleTimeExcessConnections(config.getMaxIdleTimeExcessConnections());
        dataSource.setMaxConnectionAge(config.getMaxConnectionAge());
        dataSource.setUnreturnedConnectionTimeout(config.getUnreturnedConnectionTimeout());
        dataSource.setStatementCacheNumDeferredCloseThreads(config.getStatementCacheNumDeferredCloseThreads());
        dataSource.setNumHelperThreads(config.getNumHelperThreads());

        dataSource.setPreferredTestQuery("select 1");
        dataSource.setConnectionTesterClassName(StroomProperties.getProperty("stroom.statistics.connectionTesterClassName"));
        dataSource.setDescription("SQL statistics data source");
        return dataSource;
    }

    private Flyway flyway(final DataSource dataSource) {
        final String jpaHbm2DdlAuto = StroomProperties.getProperty("stroom.jpaHbm2DdlAuto", "validate");
        if (!"update".equals(jpaHbm2DdlAuto)) {
            final Flyway flyway = new Flyway();
            flyway.setDataSource(dataSource);
            flyway.setLocations("stroom/statistics/sql/db/migration/mysql");

            Version version = null;
            boolean usingFlyWay = false;
            LOGGER.info("Testing installed statistics schema version");

            try {
                try (final Connection connection = dataSource.getConnection()) {
                    try (final Statement statement = connection.createStatement()) {
                        try (final ResultSet resultSet = statement.executeQuery("SELECT version FROM schema_version ORDER BY installed_rank DESC")) {
                            if (resultSet.next()) {
                                usingFlyWay = true;

                                final String ver = resultSet.getString(1);
                                final String[] parts = ver.split("\\.");
                                int maj = 0;
                                int min = 0;
                                int pat = 0;
                                if (parts.length > 0) {
                                    maj = Integer.valueOf(parts[0]);
                                }
                                if (parts.length > 1) {
                                    min = Integer.valueOf(parts[1]);
                                }
                                if (parts.length > 2) {
                                    pat = Integer.valueOf(parts[2]);
                                }

                                version = new Version(maj, min, pat);
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                LOGGER.debug(e.getMessage());
                // Ignore.
            }

            if (version != null) {
                LOGGER.info("Detected current statistics version is v" + version.toString());
            } else {
                LOGGER.info("This is a new statistics installation!");
            }


            if (version == null) {
                // If we have no version then this is a new statistics instance so perform full FlyWay migration.
                flyway.migrate();
            } else if (usingFlyWay) {
                // If we are already using FlyWay then allow FlyWay to attempt migration.
                flyway.migrate();
            } else {
                final String message = "The current statistics version cannot be upgraded to v5+. You must be on v4.0.60 or later.";
                LOGGER.error(MarkerFactory.getMarker("FATAL"), message);
                throw new RuntimeException(message);
            }

            return flyway;
        }

        return null;
    }

    @Override
    public DataSource get() {
        return dataSource;
    }
}