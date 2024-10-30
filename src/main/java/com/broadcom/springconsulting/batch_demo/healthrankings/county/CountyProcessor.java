package com.broadcom.springconsulting.batch_demo.healthrankings.county;

import com.broadcom.springconsulting.batch_demo.healthrankings.county.client.CountyClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.county.exception.CountyCodeAlreadyExistsCountyProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.county.exception.NotCountyRecordCountyProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

public class CountyProcessor implements ItemProcessor<InputRow, County> {

    private static final Logger log = LoggerFactory.getLogger( CountyProcessor.class );

    private final CountyClient countyClient;

    public CountyProcessor( final CountyClient countyClient ) {

        this.countyClient = countyClient;

    }

    @Override
    public County process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( input.stateCode() == 0 && input.countyCode() == 0 ) {
            log.warn( "process : stateCode and countyCode are 0, country record, skipping" );

            throw new NotCountyRecordCountyProcessorException();
        }

        if( input.stateCode() > 0 && input.countyCode() == 0 ) {
            log.warn( "process : stateCode is greater than 0 and countyCode is 0, state record, skipping" );

            throw new NotCountyRecordCountyProcessorException();
        }

        this.countyClient.findById( input.countyCode() )
                .ifPresent( country -> { throw new CountyCodeAlreadyExistsCountyProcessorException(); } );

        // countyCode and stateCode are greater than 0 && countyCode doesn't already exist
        var county = new County(
                input.countyCode(), input.county(),
                null != input.fipsCode() ? input.fipsCode() : 0L,
                input.stateCode()
        );
        log.debug( "process : County [{}]", county );

        return county;
    }

}
