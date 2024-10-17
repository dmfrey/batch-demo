package com.broadcom.springconsulting.batch_demo.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseConnectionDetails;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration( proxyBeanMethods = false )
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger( DataSourceConfig.class );

    @Bean
    @Primary
    public DataSource taskdataSource( @Qualifier( "jdbcConnectionDetailsForBatchDemoTaskdb1" ) JdbcConnectionDetails taskdb ) {

        log.info( "taskdb: {}", taskdb );

        return DataSourceBuilder.create()
                .driverClassName( taskdb.getDriverClassName() )
                .url( taskdb.getJdbcUrl() )
                .password( taskdb.getPassword() )
                .username( taskdb.getUsername() )
                .build();
    }

    @Bean
    @LiquibaseDataSource
    public DataSource healthRankingsDataSource( @Qualifier( "jdbcConnectionDetailsForBatchDemoHealthrankingsdb1" ) JdbcConnectionDetails healthrankingsdb ) {

        log.info( "healthrankingsdb: {}", healthrankingsdb );

        return DataSourceBuilder.create()
                .driverClassName( healthrankingsdb.getDriverClassName() )
                .url( healthrankingsdb.getJdbcUrl() )
                .password( healthrankingsdb.getPassword() )
                .username( healthrankingsdb.getUsername() )
                .build();
    }

    @Bean
    JdbcTemplate healthRankingsJdbcTemplate( @Qualifier( "healthRankingsDataSource" ) DataSource healthrankingsDataSource ) {

        return new JdbcTemplate( healthrankingsDataSource );
    }

    @Bean
    @Primary
    LiquibaseConnectionDetails healthRankingsLiquibaseConnectionDetails( @Qualifier( "jdbcConnectionDetailsForBatchDemoHealthrankingsdb1" ) JdbcConnectionDetails healthrankingsdb ) {

        return new JdbcConnectionDetailsLiquibaseConnectionDetails( healthrankingsdb );
    }

    static final class JdbcConnectionDetailsLiquibaseConnectionDetails implements LiquibaseConnectionDetails {

        private final JdbcConnectionDetails jdbcConnectionDetails;

        JdbcConnectionDetailsLiquibaseConnectionDetails( JdbcConnectionDetails jdbcConnectionDetails ) {
            this.jdbcConnectionDetails = jdbcConnectionDetails;
        }

        @Override
        public String getUsername() {

            return this.jdbcConnectionDetails.getUsername();
        }

        @Override
        public String getPassword() {

            return this.jdbcConnectionDetails.getPassword();
        }

        @Override
        public String getJdbcUrl() {

            return this.jdbcConnectionDetails.getJdbcUrl();
        }

        @Override
        public String getDriverClassName() {

            String driverClassName = this.jdbcConnectionDetails.getDriverClassName();

            return ( driverClassName != null ) ? driverClassName : LiquibaseConnectionDetails.super.getDriverClassName();
        }

    }

}
