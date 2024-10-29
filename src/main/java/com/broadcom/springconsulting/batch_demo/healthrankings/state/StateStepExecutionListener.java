package com.broadcom.springconsulting.batch_demo.healthrankings.state;

import com.broadcom.springconsulting.batch_demo.healthrankings.state.client.StateClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class StateStepExecutionListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger( StateStepExecutionListener.class );

    private final StateClient stateClient;

    public StateStepExecutionListener( final StateClient stateClient ) {

        this.stateClient = stateClient;

    }

    @Override
    public ExitStatus afterStep( StepExecution stepExecution ) {

        if( stepExecution.getStatus() == BatchStatus.COMPLETED ) {

            this.stateClient.findAll()
                    .forEach( state -> log.info( "Found <{{}}> in the database.", state ) );

        }

        return StepExecutionListener.super.afterStep( stepExecution );
    }

}
