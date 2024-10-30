package com.broadcom.springconsulting.batch_demo.healthrankings.countymeasure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class CountyMeasureSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger( CountyMeasureSkipPolicy.class );

    @Override
    public boolean shouldSkip( Throwable t, long skipCount ) throws SkipLimitExceededException {

        log.debug( "shouldSkip : current skipCount [{}]", skipCount );

        if( t instanceof MeasureIdRequiredCountyMeasureProcessorException) {
            log.info( "Measure Id required, skipping" );

            return true;
        }

        if( t instanceof NotCountyMeasureRecordCountyMeasureProcessorException) {
            log.info( "Not a County Measure record, skipping" );

            return true;
        }

        return false;
    }

}
