package com.broadcom.springconsulting.batch_demo.healthrankings.measure;

import com.broadcom.springconsulting.batch_demo.healthrankings.measure.exception.MeasureIdAlreadyExistsMeasureProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.measure.exception.MeasureIdRequiredMeasureProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;

public class MeasureProcessor implements ItemProcessor<InputRow, Measure> {

    private static final Logger log = LoggerFactory.getLogger( MeasureProcessor.class );

    private final JdbcTemplate jdbcTemplate;

    public MeasureProcessor( final JdbcTemplate jdbcTemplate ) {

        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public Measure process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.measureId() ) {
            log.error( "process : measureId is null, skipping" );

            throw new MeasureIdRequiredMeasureProcessorException();
        }

        var existing =
                this.jdbcTemplate.queryForObject(
                        "SELECT count(measure_id) FROM measure WHERE measure_id = ?", Integer.class, input.measureId() );
        if( null != existing && !existing.equals( 0 ) ) {

            throw new MeasureIdAlreadyExistsMeasureProcessorException();
        }

        var measure = new Measure( input.measureId(), input.measureName() );
        log.debug( "process : Measure [{}]", measure );

        return measure;
    }

}
