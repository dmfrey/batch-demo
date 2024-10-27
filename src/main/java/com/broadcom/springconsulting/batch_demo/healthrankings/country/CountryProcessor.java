package com.broadcom.springconsulting.batch_demo.healthrankings.country;

import com.broadcom.springconsulting.batch_demo.healthrankings.country.exception.CountryCodeAlreadyExistsCountryProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.exception.CountryCodeRequiredCountryProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.exception.NotCountryRecordCountryProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;

public class CountryProcessor implements ItemProcessor<InputRow, Country> {

    private static final Logger log = LoggerFactory.getLogger( CountryProcessor.class );

    private final JdbcTemplate jdbcTemplate;

    public CountryProcessor( final JdbcTemplate jdbcTemplate ) {

        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public Country process( @NonNull InputRow input ) throws Exception {

        log.debug( "process : InputRow [{}]", input );
        if( null == input.stateCode() || null == input.countyCode() ) {
            log.error( "process : stateCode or countyCode is null, skipping" );

            throw new CountryCodeRequiredCountryProcessorException();
        }

        if( !input.stateCode().equals( 0L ) && !input.countyCode().equals( 0L ) ) {
            log.warn( "process : stateCode or countyCode are not 0, not a country, skipping" );

            throw new NotCountryRecordCountryProcessorException();
        }

        var existing =
                this.jdbcTemplate.queryForObject(
                        "SELECT count(country_code) FROM country WHERE country_code = ?", Integer.class, input.stateCode() );
        if( null != existing && !existing.equals( 0 ) ) {

            throw new CountryCodeAlreadyExistsCountryProcessorException();
        }

        var country = new Country(
                input.stateCode(), input.state(), input.county(),
                null != input.fipsCode() ? input.fipsCode() : 0L
        );
        log.debug( "process : Country [{}]", country );

        return country;
    }

}
