package com.broadcom.springconsulting.batch_demo.healthrankings.county.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class CountySkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger( CountySkipPolicy.class );

    @Override
    public boolean shouldSkip( Throwable t, long skipCount ) throws SkipLimitExceededException {

        log.debug( "shouldSkip : current skipCount [{}]", skipCount );

        if( t instanceof CountyCodeAlreadyExistsCountyProcessorException) {
            log.info( "County Code already exists, skipping" );

            return true;
        }

        if( t instanceof NotCountyRecordCountyProcessorException ) {

            return true;
        }

        return false;
    }

}
