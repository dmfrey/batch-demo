package com.broadcom.springconsulting.batch_demo.healthrankings.county.client;

import com.broadcom.springconsulting.batch_demo.healthrankings.county.County;

import java.util.List;
import java.util.Optional;

public interface CountyClient {

    List<County> findAll();

    Optional<County> findById( long countyCode );

    void create( County county );

    void update( County county, long countyCode );

    void delete( long countyCode );

    int countByStateCode( long stateCode );

}
