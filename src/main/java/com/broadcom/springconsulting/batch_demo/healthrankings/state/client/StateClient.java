package com.broadcom.springconsulting.batch_demo.healthrankings.state.client;

import com.broadcom.springconsulting.batch_demo.healthrankings.state.State;

import java.util.List;
import java.util.Optional;

public interface StateClient {

    List<State> findAll();

    Optional<State> findById( long stateCode );

    void create( State state );

    void update( State state, long stateCode );

    void delete( long stateCode );

}
