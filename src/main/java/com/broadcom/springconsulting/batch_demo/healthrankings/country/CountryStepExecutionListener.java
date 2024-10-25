package com.broadcom.springconsulting.batch_demo.healthrankings.country;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

public class CountryStepExecutionListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger( CountryStepExecutionListener.class );

    private final JdbcTemplate jdbcTemplate;

    public CountryStepExecutionListener(final JdbcTemplate jdbcTemplate ) {

        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public ExitStatus afterStep( StepExecution stepExecution ) {

        if( stepExecution.getStatus() == BatchStatus.COMPLETED ) {

            jdbcTemplate
                    .query( "SELECT country_code, abbreviation, name, fips_code FROM country", new DataClassRowMapper<>( Country.class ) )
                    .forEach( country -> log.info( "Found <{{}}> in the database.", country ) );

        }

        return StepExecutionListener.super.afterStep( stepExecution );
    }

}
