package com.broadcom.springconsulting.batch_demo.healthrankings.county;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CountyProcessor implements ItemProcessor<InputRow, County> {

    private static final Logger log = LoggerFactory.getLogger( CountyProcessor.class );

    @Override
    public County process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.countyCode() ) {
            log.error( "process : countyCode is null, skipping" );

            return null;
        }

        var county = new County(
                input.countyCode(), input.county(),
                null != input.fipsCode() ? input.fipsCode() : 0L,
                input.stateCode()
        );
        log.debug( "process : County [{}]", county );

        return county;
    }

}
