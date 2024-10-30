package com.broadcom.springconsulting.batch_demo.healthrankings.country.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class CountrySkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger( CountrySkipPolicy.class );

    @Override
    public boolean shouldSkip( Throwable t, long skipCount ) throws SkipLimitExceededException {

        log.debug( "shouldSkip : current skipCount [{}]", skipCount );

        if( t instanceof CountryCodeAlreadyExistsCountryProcessorException ) {
            log.info( "Country Code already exists, skipping" );

            return true;
        }

        if( t instanceof NotCountryRecordCountryProcessorException ) {
            log.info( "Not a Country record, skipping" );

            return true;
        }

        return false;
    }

}
