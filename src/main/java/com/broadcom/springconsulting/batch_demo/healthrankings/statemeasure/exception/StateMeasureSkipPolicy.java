package com.broadcom.springconsulting.batch_demo.healthrankings.statemeasure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class StateMeasureSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger( StateMeasureSkipPolicy.class );

    @Override
    public boolean shouldSkip( Throwable t, long skipCount ) throws SkipLimitExceededException {

        log.debug( "shouldSkip : current skipCount [{}]", skipCount );

        if( t instanceof MeasureIdRequiredStateMeasureProcessorException) {
            log.info( "Measure Id required, skipping" );

            return true;
        }

        if( t instanceof NotStateMeasureRecordStateMeasureProcessorException) {
            log.info( "Not a State Measure record, skipping" );

            return true;
        }

        if( t instanceof StateCodeRequiredStateMeasureProcessorException) {
            log.info( "State Code required, skipping" );

            return true;
        }

        return false;
    }

}
