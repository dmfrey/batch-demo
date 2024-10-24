package com.broadcom.springconsulting.batch_demo.healthrankings.country;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

public class CountryProcessor implements ItemProcessor<InputRow, Country> {

    private static final Logger log = LoggerFactory.getLogger( CountryProcessor.class );

    @Override
    public Country process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.stateCode() || null == input.countyCode() ) {
            log.error( "process : stateCode or countyCode is null, skipping" );

            return null;
        }

        if( !input.stateCode().equals( 0L ) && !input.countyCode().equals( 0L ) ) {
            log.warn( "process : stateCode or countyCode are not 0, not a country, skipping" );

            return null;
        }

        var country = new Country(
                input.stateCode(), input.state(), input.county(),
                null != input.fipsCode() ? input.fipsCode() : 0L
        );
        log.debug( "process : Country [{}]", country );

        return country;
    }

}
