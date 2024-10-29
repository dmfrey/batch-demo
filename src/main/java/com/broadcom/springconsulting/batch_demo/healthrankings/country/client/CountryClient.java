package com.broadcom.springconsulting.batch_demo.healthrankings.country.client;

import com.broadcom.springconsulting.batch_demo.healthrankings.country.Country;

import java.util.List;
import java.util.Optional;

public interface CountryClient {

    List<Country> findAll();

    Optional<Country> findById( long countryCode );

    void create( Country country );

    void update( Country country, long countryCode );

    void delete( long countryCode );

}
