package com.broadcom.springconsulting.batch_demo.healthrankings.county;

import com.broadcom.springconsulting.batch_demo.healthrankings.county.exception.CountyIdAlreadyExistsCountyProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.county.exception.CountyIdRequiredCountyProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;

public class CountyProcessor implements ItemProcessor<InputRow, County> {

    private static final Logger log = LoggerFactory.getLogger( CountyProcessor.class );

    private final JdbcTemplate jdbcTemplate;
    public CountyProcessor( final JdbcTemplate jdbcTemplate ) {

        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public County process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.countyCode() ) {
            log.error( "process : countyCode is null, skipping" );

            throw new CountyIdRequiredCountyProcessorException();
        }

        var existing =
                this.jdbcTemplate.queryForObject(
                        "SELECT count(county_code) FROM county WHERE county_code = ?", Integer.class, input.countyCode() );
        if( null != existing && !existing.equals( 0 ) ) {

            throw new CountyIdAlreadyExistsCountyProcessorException();
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
