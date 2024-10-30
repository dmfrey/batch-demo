package com.broadcom.springconsulting.batch_demo.healthrankings.county.client;

import com.broadcom.springconsulting.batch_demo.healthrankings.county.County;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

public class CountyClientJdbcClient implements CountyClient {

    private static final Logger log = LoggerFactory.getLogger( CountyClientJdbcClient.class );

    private final JdbcClient jdbcClient;

    public CountyClientJdbcClient( final JdbcClient jdbcClient ) {

        this.jdbcClient = jdbcClient;

    }

    @Override
    public List<County> findAll() {
        log.debug( "findAll : enter" );

        return this.jdbcClient.sql( "SELECT county_code, name, fips_code, state_code FROM county" )
                .query( County.class )
                .list();
    }

    @Override
    public Optional<County> findById( final long countyCode ) {
        log.debug( "findById : enter" );

        return this.jdbcClient.sql( "SELECT county_code, name, fips_code, state_code FROM county WHERE county_code = :countyCode" )
                .param( "countyCode", countyCode )
                .query( County.class )
                .optional();
    }

    @Override
    public void create( final County county ) {
        log.debug( "create : enter" );

        int created = this.jdbcClient.sql( "INSERT INTO county (county_code, name, fips_code, state_code) VALUES (?,?,?,?)" )
                .params( List.of( county.countyCode(), county.name(), county.fipsCode(), county.stateCode() ) )
                .update();

        Assert.state( created == 1, "Failed to create county [" + county.countyCode() + "]" );

        log.debug( "create : exit" );
    }

    @Override
    public void update( final County county, final long countyCode ) {
        log.debug( "update : enter" );

        int updated = this.jdbcClient.sql( "UPDATE county SET name = ?, fips_code = ?, state_code = ? WHERE county_code = ?" )
                .params( List.of( county.name(), county.fipsCode(), county.stateCode(), countyCode ) )
                .update();

        Assert.state( updated == 1, "Failed to update county [" + county.countyCode() + "]" );

        log.debug( "update : exit" );
    }

    @Override
    public void delete( final long countyCode ) {
        log.debug( "delete : enter" );

        var deleted = jdbcClient.sql( "delete from county where county_code = :countyCode" )
                .param( "countyCode", countyCode )
                .update();

        Assert.state(deleted == 1, "Failed to delete county [" + countyCode + "]" );

        log.debug( "delete : exit" );
    }

    @Override
    public int countByStateCode( final long stateCode ) {

        return jdbcClient.sql( "SELECT count(name) FROM county WHERE state_code = :stateCode" )
                .param( "stateCode", stateCode )
                .query( int.class )
                .single();
    }

}
