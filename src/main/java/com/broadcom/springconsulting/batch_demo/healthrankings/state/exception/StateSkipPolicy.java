package com.broadcom.springconsulting.batch_demo.healthrankings.state.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class StateSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger( StateSkipPolicy.class );

    @Override
    public boolean shouldSkip( Throwable t, long skipCount ) throws SkipLimitExceededException {

        log.debug( "shouldSkip : current skipCount [{}]", skipCount );

        if( t instanceof StateCodeAlreadyExistsStateProcessorException) {
            log.info( "State Code already exists, skipping" );

            return true;
        }

        if( t instanceof NotStateRecordStateProcessorException) {
            log.info( "Not a State record, skipping" );

            return true;
        }

        return false;
    }

}
