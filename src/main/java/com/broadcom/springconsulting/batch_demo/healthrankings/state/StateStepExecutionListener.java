package com.broadcom.springconsulting.batch_demo.healthrankings.state;

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
public class StateStepExecutionListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger( StateStepExecutionListener.class );

    private final JdbcTemplate healthrankingsDataSource;

    public StateStepExecutionListener( final JdbcTemplate healthrankingsDataSource ) {

        this.healthrankingsDataSource = healthrankingsDataSource;

    }

    @Override
    public ExitStatus afterStep( StepExecution stepExecution ) {

        if( stepExecution.getStatus() == BatchStatus.COMPLETED ) {

            healthrankingsDataSource
                    .query( "SELECT state_code, abbreviation FROM state ORDER BY abbreviation", new DataClassRowMapper<>( State.class ) )
                    .forEach( state -> log.info( "Found <{{}}> in the database.", state ) );

        }

        return StepExecutionListener.super.afterStep( stepExecution );
    }

}
