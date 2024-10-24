package com.broadcom.springconsulting.batch_demo.healthrankings.state;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

public class StateProcessor implements ItemProcessor<InputRow, State> {

    private static final Logger log = LoggerFactory.getLogger( StateProcessor.class );

    @Override
    public State process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.stateCode() ) {
            log.error( "process : stateCode is null, skipping" );

            return null;
        }

        if( input.countyCode() != 0 ) {
            log.warn( "process : countyCode is not 0, skipping" );

            return null;
        }

        var state = new State( input.stateCode(), input.state(), input.county(), input.fipsCode() );
        log.debug( "process : State [{}]", state );

        return state;
    }

}
