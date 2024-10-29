package com.broadcom.springconsulting.batch_demo.healthrankings.measure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class MeasureSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger( MeasureSkipPolicy.class );

    @Override
    public boolean shouldSkip( Throwable t, long skipCount ) throws SkipLimitExceededException {

        log.debug( "shouldSkip : current skipCount [{}]", skipCount );

        if( t instanceof MeasureIdAlreadyExistsMeasureProcessorException) {
            log.info( "Measure Id already exists, skipping" );

            return true;
        }

        if( t instanceof MeasureIdRequiredMeasureProcessorException) {
            log.info( "Measure Id required, skipping" );

            return true;
        }

        return false;
    }

}
