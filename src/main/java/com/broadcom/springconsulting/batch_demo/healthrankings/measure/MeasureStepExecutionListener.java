package com.broadcom.springconsulting.batch_demo.healthrankings.measure;

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
public class MeasureStepExecutionListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger( MeasureStepExecutionListener.class );

    private final JdbcTemplate jdbcTemplate;

    public MeasureStepExecutionListener(final JdbcTemplate jdbcTemplate ) {

        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public ExitStatus afterStep( StepExecution stepExecution ) {

        if( stepExecution.getStatus() == BatchStatus.COMPLETED ) {

            jdbcTemplate
                    .query( "SELECT measure_id, name FROM measure ORDER BY name", new DataClassRowMapper<>( Measure.class ) )
                    .forEach( measure -> log.info( "Found <{{}}> in the database.", measure ) );

        }

        return StepExecutionListener.super.afterStep( stepExecution );
    }

}
