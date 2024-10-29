package com.broadcom.springconsulting.batch_demo.healthrankings.country.client;

import com.broadcom.springconsulting.batch_demo.healthrankings.country.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

public class CountryClientJdbcClient implements CountryClient {

    private static final Logger log = LoggerFactory.getLogger( CountryClientJdbcClient.class );

    private final JdbcClient jdbcClient;

    public CountryClientJdbcClient( final JdbcClient jdbcClient ) {

        this.jdbcClient = jdbcClient;

    }

    @Override
    public List<Country> findAll() {
        log.debug( "findAll : enter" );

        return this.jdbcClient.sql( "SELECT country_code, abbreviation, name, fips_code FROM country" )
                .query( Country.class )
                .list();
    }

    @Override
    public Optional<Country> findById( final long countryCode ) {
        log.debug( "findById : enter" );

        return this.jdbcClient.sql( "SELECT country_code, abbreviation, name, fips_code FROM country WHERE country_code = :countryCode" )
                .param( "countryCode", countryCode )
                .query( Country.class )
                .optional();
    }

    @Override
    public void create( final Country country ) {
        log.debug( "create : enter" );

        int created = this.jdbcClient.sql( "INSERT INTO country (country_code, abbreviation, name, fips_code) VALUES (?,?,?,?)" )
                .params( List.of( country.countryCode(), country.abbreviation(), country.name(), country.fipsCode() ) )
                .update();

        Assert.state( created == 1, "Failed to create country [" + country.countryCode() + "]" );

        log.debug( "create : exit" );
    }

    @Override
    public void update( final Country country, final long countryCode ) {
        log.debug( "update : enter" );

        int updated = this.jdbcClient.sql( "UPDATE country SET abbreviation = ?, name = ?, fips_code = ? WHERE country_code = ?" )
                .params( List.of( country.abbreviation(), country.name(), country.fipsCode(), countryCode ) )
                .update();

        Assert.state( updated == 1, "Failed to update country [" + country.countryCode() + "]" );

        log.debug( "update : exit" );
    }

    @Override
    public void delete( final long countryCode ) {
        log.debug( "delete : enter" );

        var deleted = jdbcClient.sql( "delete from country where country_code = :countryCode" )
                .param( "countryCode", countryCode )
                .update();

        Assert.state(deleted == 1, "Failed to delete country " + countryCode);

        log.debug( "delete : exit" );
    }

}
