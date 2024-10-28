package com.broadcom.springconsulting.batch_demo.healthrankings.county;

import com.broadcom.springconsulting.batch_demo.healthrankings.county.client.CountyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class CountyStepExecutionListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger( CountyStepExecutionListener.class );

    private final CountyClient countyClient;

    public CountyStepExecutionListener( final CountyClient countyClient ) {

        this.countyClient = countyClient;

    }

    @Override
    public ExitStatus afterStep( StepExecution stepExecution ) {

        if( stepExecution.getStatus() == BatchStatus.COMPLETED ) {

            this.countyClient.findAll()
                    .forEach( county -> log.info( "Found <{{}}> in the database.", county ) );

        }

        return StepExecutionListener.super.afterStep( stepExecution );
    }

}
