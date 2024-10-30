package com.broadcom.springconsulting.batch_demo.healthrankings.state;

import com.broadcom.springconsulting.batch_demo.healthrankings.state.client.StateClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.exception.NotStateRecordStateProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.exception.StateCodeAlreadyExistsStateProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

public class StateProcessor implements ItemProcessor<InputRow, State> {

    private static final Logger log = LoggerFactory.getLogger( StateProcessor.class );

    final StateClient stateClient;

    public StateProcessor( final StateClient stateClient ) {

        this.stateClient = stateClient;

    }

    @Override
    public State process( @NonNull InputRow input ) throws Exception {

        log.info( "process : InputRow [{}]", input );
        if( input.stateCode() == 0 && input.countyCode() == 0 ) {
            log.warn( "process : stateCode and countyCode are 0, country record, skipping" );

            throw new NotStateRecordStateProcessorException();
        }

        if( input.stateCode() > 0 && input.countyCode() > 0 ) {
            log.warn( "process : stateCode and countyCode are greater than 0, county record, skipping" );

            throw new NotStateRecordStateProcessorException();
        }

        this.stateClient.findById( input.stateCode() )
                .ifPresent( country -> { throw new StateCodeAlreadyExistsStateProcessorException(); } );

        // stateCode > 0 && countyCode == 0 && State does not already exist
        var state = new State( input.stateCode(), input.state(), input.county(), input.fipsCode() );
        log.debug( "process : State [{}]", state );

        return state;
    }

}
