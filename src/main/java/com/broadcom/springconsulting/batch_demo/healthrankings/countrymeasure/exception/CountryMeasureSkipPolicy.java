package com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class CountryMeasureSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger( CountryMeasureSkipPolicy.class );

    @Override
    public boolean shouldSkip( Throwable t, long skipCount ) throws SkipLimitExceededException {

        log.debug( "shouldSkip : current skipCount [{}]", skipCount );

        if( t instanceof MeasureIdRequiredCountryMeasureProcessorException) {
            log.info( "Measure Id required, skipping" );

            return true;
        }

        if( t instanceof NotCountryMeasureRecordCountryMeasureProcessorException) {
            log.info( "Not a Country Measure record, skipping" );

            return true;
        }

        if( t instanceof CountryCodeRequiredCountryMeasureProcessorException) {
            log.info( "Country Code required, skipping" );

            return true;
        }

        return false;
    }

}
