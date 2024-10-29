package com.broadcom.springconsulting.batch_demo.healthrankings.state.client;

import com.broadcom.springconsulting.batch_demo.healthrankings.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

public class StateClientJdbcClient implements StateClient {

    private static final Logger log = LoggerFactory.getLogger( StateClientJdbcClient.class );

    private final JdbcClient jdbcClient;

    public StateClientJdbcClient( final JdbcClient jdbcClient ) {

        this.jdbcClient = jdbcClient;

    }

    @Override
    public List<State> findAll() {
        log.debug( "findAll : enter" );

        return this.jdbcClient.sql( "SELECT state_code, abbreviation, name, fips_code FROM state" )
                .query( State.class )
                .list();
    }

    @Override
    public Optional<State> findById( final long stateCode ) {
        log.debug( "findById : enter" );

        return this.jdbcClient.sql( "SELECT state_code, abbreviation, name, fips_code FROM state WHERE state_code = :stateCode" )
                .param( "stateCode", stateCode )
                .query( State.class )
                .optional();
    }

    @Override
    public void create( final State state ) {
        log.debug( "create : enter" );

        int created = this.jdbcClient.sql( "INSERT INTO state (state_code, abbreviation, name, fips_code) VALUES (?,?,?,?)" )
                .params( List.of( state.stateCode(), state.abbreviation(), state.name(), state.fipsCode() ) )
                .update();

        Assert.state( created == 1, "Failed to create state [" + state.stateCode() + "]" );

        log.debug( "create : exit" );
    }

    @Override
    public void update( final State state, final long stateCode ) {
        log.debug( "update : enter" );

        int updated = this.jdbcClient.sql( "UPDATE state SET abbreviation = ?, name = ?, fips_code = ? WHERE state_code = ?" )
                .params( List.of( state.abbreviation(), state.name(), state.fipsCode(), stateCode ) )
                .update();

        Assert.state( updated == 1, "Failed to update state [" + state.stateCode() + "]" );

        log.debug( "update : exit" );
    }

    @Override
    public void delete( final long stateCode ) {
        log.debug( "delete : enter" );

        var deleted = jdbcClient.sql( "delete from state where state_code = :stateCode" )
                .param( "stateCode", stateCode )
                .update();

        Assert.state(deleted == 1, "Failed to delete state [" + stateCode + "]" );

        log.debug( "delete : exit" );
    }

}
