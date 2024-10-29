package com.broadcom.springconsulting.batch_demo.healthrankings.statemeasure;

import com.broadcom.springconsulting.batch_demo.healthrankings.statemeasure.exception.MeasureIdRequiredStateMeasureProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.statemeasure.exception.NotStateMeasureRecordStateMeasureProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

import java.util.UUID;

public class StateMeasureProcessor implements ItemProcessor<InputRow, StateMeasure> {

    private static final Logger log = LoggerFactory.getLogger( StateMeasureProcessor.class );

    @Override
    public StateMeasure process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.measureId() ) {
            log.error( "process : measureId is null, skipping" );

            throw new MeasureIdRequiredStateMeasureProcessorException();
        }

        if( input.stateCode().equals( 0L ) ) {
            log.warn( "process : stateCode is 0, country measure, skipping" );

            throw new NotStateMeasureRecordStateMeasureProcessorException();
        }

        if( !input.countyCode().equals( 0L ) ) {
            log.warn( "process : countyCode is not 0, not a state measure, skipping" );

            throw new NotStateMeasureRecordStateMeasureProcessorException();
        }

        var stateMeasure = new StateMeasure(
                UUID.randomUUID(), input.yearSpan(),
                null != input.numerator() ? input.numerator() : 0.00d,
                null != input.denominator() ? input.denominator() : 0.00d,
                null != input.rawValue() ? input.rawValue() : 0.00d,
                null != input.confidenceIntervalLowerBound() ? input.confidenceIntervalLowerBound() : 0.00d,
                null != input.confidenceIntervalUpperBound() ? input.confidenceIntervalUpperBound() : 0.00d,
                input.dataReleaseYear(), input.stateCode(), input.measureId()
        );
        log.debug( "process : StateMeasure [{}]", stateMeasure );

        return stateMeasure;
    }

}
