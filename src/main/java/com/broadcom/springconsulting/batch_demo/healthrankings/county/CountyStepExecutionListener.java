package com.broadcom.springconsulting.batch_demo.healthrankings.county;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CountyStepExecutionListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger( CountyStepExecutionListener.class );

    private final JdbcTemplate healthrankingsDataSource;

    public CountyStepExecutionListener( final JdbcTemplate healthrankingsDataSource ) {

        this.healthrankingsDataSource = healthrankingsDataSource;

    }

    @Override
    public ExitStatus afterStep( StepExecution stepExecution ) {

        if( stepExecution.getStatus() == BatchStatus.COMPLETED ) {

            healthrankingsDataSource
                    .query( "SELECT county_code, name, fips_code, state_code FROM county", new DataClassRowMapper<>( County.class ) )
                    .forEach( county -> log.info( "Found <{{}}> in the database.", county ) );

        }

        return StepExecutionListener.super.afterStep( stepExecution );
    }

}
