package com.broadcom.springconsulting.batch_demo.job;

import com.broadcom.springconsulting.batch_demo.TestcontainersConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.Country;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.CountryConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.client.CountryClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure.CountryMeasureConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.county.County;
import com.broadcom.springconsulting.batch_demo.healthrankings.county.CountyConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.county.client.CountyClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.countymeasure.CountyMeasureConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.measure.MeasureConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.State;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.StateConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.client.StateClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.statemeasure.StateMeasureConfiguration;
import com.broadcom.springconsulting.batch_demo.input.ReaderConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import static com.broadcom.springconsulting.batch_demo.healthrankings.TestUtils.defaultJobParameters;
import static org.assertj.core.api.Assertions.assertThat;

@Import({
        TestcontainersConfiguration.class,
        CountryConfiguration.class, CountryMeasureConfiguration.class,
        StateConfiguration.class, StateMeasureConfiguration.class,
        CountyConfiguration.class, CountyMeasureConfiguration.class,
        MeasureConfiguration.class,
        ReaderConfiguration.class
})
@SpringBootTest
@SpringBatchTest
@SpringJUnitConfig( JobConfiguration.class )
@AutoConfigureDataJdbc
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
})
@EnableAutoConfiguration
@TestPropertySource(
        properties = {
                "spring.batch.jdbc.initialize-schema=always"
        }
)
public class JobConfigurationTests {

    private static final Logger log = LoggerFactory.getLogger( JobConfigurationTests.class );

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private CountryClient countryClient;

    @Autowired
    private StateClient stateClient;

    @Autowired
    private CountyClient countyClient;

    @AfterEach
    void cleanUp() {

        this.jobRepositoryTestUtils.removeJobExecutions();

    }

    @Test
    void testImportDataJob() throws Exception {

        var jobExecution = this.jobLauncherTestUtils.launchJob( defaultJobParameters( "src/test/resources/test-files/test-all.csv" ) );
        var actualJobInstance = jobExecution.getJobInstance();
        var actualJobExitStatus = jobExecution.getExitStatus();

        assertThat( actualJobInstance.getJobName() ).isEqualTo( "importDataJob" );
        assertThat( actualJobExitStatus.getExitCode() ).isEqualTo( "COMPLETED" );

        var expectedCountry = new Country( 0, "US", "United States", 0 );
        var country = this.countryClient.findById( 0 );
        assertThat( country )
                .isPresent()
                .map( c -> c )
                .hasValue( expectedCountry );

        var countries = this.countryClient.findAll();
        assertThat( countries )
                .containsExactly( expectedCountry );

        this.countryClient.findAll()
                .forEach( c -> log.debug( "Country [{}]", c ) );

        var expectedState = new State( 1, "AL", "Alabama", 1000 );
        var state = this.stateClient.findById( 1 );
        assertThat( state )
                .isPresent()
                .map( s -> s )
                .hasValue( expectedState );

        var states = this.stateClient.findAll();
        assertThat( states )
                .containsExactly( expectedState );

        this.stateClient.findAll()
                .forEach( s -> log.debug( "State [{}]", s ) );

        var expectedCounty = new County( 1, "Autauga County", 1001, 1 );
        var county = this.countyClient.findById( 1 );
        assertThat( county )
                .isPresent()
                .map( c -> c )
                .hasValue( expectedCounty );

        var countyCount = this.countyClient.countByStateCode( 1 );
        assertThat( countyCount ).isEqualTo( 67 );

        this.countyClient.findAll()
                .forEach( c -> log.debug( "County [{}]", c ) );

    }

}
