package com.broadcom.springconsulting.batch_demo.healthrankings.measure;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

public class MeasureProcessor implements ItemProcessor<InputRow, Measure> {

    private static final Logger log = LoggerFactory.getLogger( MeasureProcessor.class );

    @Override
    public Measure process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.measureId() ) {
            log.error( "process : measureId is null, skipping" );

            return null;
        }

        var measure = new Measure( input.measureId(), input.measureName() );
        log.debug( "process : Measure [{}]", measure );

        return measure;
    }

}
