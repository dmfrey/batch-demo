package com.broadcom.springconsulting.batch_demo.healthrankings.state;

import com.broadcom.springconsulting.batch_demo.healthrankings.state.exception.NotStateRecordStateProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.exception.StateCodeAlreadyExistsStateProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.exception.StateCodeRequiredStateProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;

public class StateProcessor implements ItemProcessor<InputRow, State> {

    private static final Logger log = LoggerFactory.getLogger( StateProcessor.class );

    final JdbcTemplate jdbcTemplate;

    public StateProcessor( final JdbcTemplate jdbcTemplate ) {

        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public State process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.stateCode() ) {
            log.error( "process : stateCode is null, skipping" );

            throw new StateCodeRequiredStateProcessorException();
        }

        if( input.countyCode() != 0 ) {
            log.warn( "process : countyCode is not 0, skipping" );

            throw new NotStateRecordStateProcessorException();
        }

        var existing =
                this.jdbcTemplate.queryForObject(
                        "SELECT count(state_code) FROM state WHERE state_code = ?", Integer.class, input.stateCode() );
        if( null != existing && !existing.equals( 0 ) ) {

            throw new StateCodeAlreadyExistsStateProcessorException();
        }

        var state = new State( input.stateCode(), input.state(), input.county(), input.fipsCode() );
        log.debug( "process : State [{}]", state );

        return state;
    }

}
