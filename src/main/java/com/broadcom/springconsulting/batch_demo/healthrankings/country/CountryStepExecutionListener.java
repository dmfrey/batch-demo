package com.broadcom.springconsulting.batch_demo.healthrankings.country;

import com.broadcom.springconsulting.batch_demo.healthrankings.country.client.CountryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class CountryStepExecutionListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger( CountryStepExecutionListener.class );

    private final CountryClient countryClient;

    public CountryStepExecutionListener( final CountryClient countryClient ) {

        this.countryClient = countryClient;

    }

    @Override
    public ExitStatus afterStep( StepExecution stepExecution ) {

        if( stepExecution.getStatus() == BatchStatus.COMPLETED ) {

            this.countryClient.findAll()
                    .forEach( country -> log.info( "Found <{{}}> in the database.", country ) );

        }

        return StepExecutionListener.super.afterStep( stepExecution );
    }

}
