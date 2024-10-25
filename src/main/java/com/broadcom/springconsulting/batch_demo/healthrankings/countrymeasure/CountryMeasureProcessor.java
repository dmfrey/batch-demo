package com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

import java.util.UUID;

public class CountryMeasureProcessor implements ItemProcessor<InputRow, CountryMeasure> {

    private static final Logger log = LoggerFactory.getLogger( CountryMeasureProcessor.class );

    @Override
    public CountryMeasure process(@NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.stateCode() || null == input.countyCode() ) {
            log.error( "process : stateCode or countyCode is null, skipping" );

            return null;
        }

        if( null == input.measureId() ) {
            log.error( "process : measureId is null, skipping" );

            return null;
        }

        if( !input.stateCode().equals( 0L ) && !input.countyCode().equals( 0L ) ) {
            log.warn( "process : stateCode and countryCode are not 0, not a country measure, skipping" );

            return null;
        }

        var countryMeasure = new CountryMeasure(
                UUID.randomUUID(), input.yearSpan(),
                null != input.numerator() ? input.numerator() : 0.00d,
                null != input.denominator() ? input.denominator() : 0.00d,
                null != input.rawValue() ? input.rawValue() : 0.00d,
                null != input.confidenceIntervalLowerBound() ? input.confidenceIntervalLowerBound() : 0.00d,
                null != input.confidenceIntervalUpperBound() ? input.confidenceIntervalUpperBound() : 0.00d,
                input.dataReleaseYear(), input.stateCode(), input.measureId()
        );
        log.debug( "process : CountryMeasure [{}]", countryMeasure );

        return countryMeasure;
    }

}
