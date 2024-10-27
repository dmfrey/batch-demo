package com.broadcom.springconsulting.batch_demo.healthrankings.country;

import com.broadcom.springconsulting.batch_demo.TestcontainersConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.exception.CountryCodeAlreadyExistsCountryProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.exception.CountryCodeRequiredCountryProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.exception.NotCountryRecordCountryProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import com.broadcom.springconsulting.batch_demo.input.ReaderConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.sql.DataSource;

import static com.broadcom.springconsulting.batch_demo.healthrankings.TestUtils.defaultJobParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({ TestcontainersConfiguration.class, ReaderConfiguration.class })
@SpringBatchTest
@SpringJUnitConfig( CountryConfiguration.class )
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        StepScopeTestExecutionListener.class
})
@AutoConfigureDataJdbc
@EnableAutoConfiguration
@TestPropertySource(
        properties = {
                "spring.batch.jdbc.initialize-schema=always"
        }
)
@DirtiesContext
public class CountryConfigurationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private FlatFileItemReader<InputRow> reader;

    @Autowired
    private CountryProcessor processor;

    @Autowired
    private JdbcBatchItemWriter<Country> writer;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource( DataSource dataSource ) {

        this.jdbcTemplate = new JdbcTemplate( dataSource );

    }

    @AfterEach
    void cleanUp() {

        jobRepositoryTestUtils.removeJobExecutions();

        this.jdbcTemplate.update( "TRUNCATE TABLE country CASCADE" );

    }

    @Test
    void testReader() {

        assertThat( this.reader ).isNotNull();

    }

    @Test
    void testCountryReaderStep() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution( defaultJobParameters( "src/test/resources/test-files/test-country.csv" ) );

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var expected =
                    new InputRow(
                            "US", "United States", 0L, 0L, "2003-2005",
                            "Violent crime rate", 43L, 1328750.667, 274877117.0,
                            483.3980657, null, null, "",
                            0L
                    );

            this.reader.open( stepExecution.getExecutionContext() );

            InputRow inputRow;
            while( ( inputRow = this.reader.read() ) != null ) {
                assertThat( inputRow ).isEqualTo( expected );
            }
            this.reader.close();

            return null;
        });

    }

    @Test
    void testCountryProcessor_whenCountryCodeIsNull_verifySkip() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution();

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeInputRow =
                    new InputRow(
                            null, null, null, null, null,
                            null, null, null, null,
                            null, null, null, "",
                            null
                    );

            assertThatThrownBy( () -> this.processor.process( fakeInputRow ) )
                    .isInstanceOf( CountryCodeRequiredCountryProcessorException.class );

            return null;
        });

    }

    @Test
    void testCountryProcessor_whenNotCountryInputRecord_verifySkip() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution();

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeInputRow =
                    new InputRow(
                            "AL", "Autauga County", 1L, 1L, "2003-2005",
                            "Violent crime rate", 43L, 141.0, 46438.66667,
                            303.6262884, null, null, "",
                            1001L
                    );

            assertThatThrownBy( () -> this.processor.process( fakeInputRow ) )
                    .isInstanceOf( NotCountryRecordCountryProcessorException.class );

            return null;
        });

    }

    @Test
    void testCountryProcessor_whenCountryIdAlreadyExists_verifySkip() throws Exception {

        this.jdbcTemplate.update( "INSERT INTO country (country_code, abbreviation, name, fips_code) VALUES (0, 'US', 'United States', 0)" );

        var stepExecution = MetaDataInstanceFactory.createStepExecution();

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeInputRow =
                    new InputRow(
                            "US", "United States", 0L, 0L, "2003-2005",
                            "Violent crime rate", 43L, 1328750.667, 274877117.0,
                            483.3980657, null, null, "",
                            0L
                    );

            assertThatThrownBy( () -> this.processor.process( fakeInputRow ) )
                    .isInstanceOf( CountryCodeAlreadyExistsCountryProcessorException.class );

            return null;
        });

    }

    @Test
    void testCountryProcessorStep() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution();

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeInputRow =
                    new InputRow(
                            "US", "United States", 0L, 0L, "2003-2005",
                            "Violent crime rate", 43L, 1328750.667, 274877117.0,
                            483.3980657, null, null, "",
                            0L
                    );

            var actual = this.processor.process( fakeInputRow );

            var expected = new Country( 0, "US", "United States", 0 );
            assertThat( actual ).isEqualTo( expected );

            return null;
        });

    }

    @Test
    void testCountryWriterStep() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution( defaultJobParameters( "src/test/resources/test-files/test-country.csv" ) );

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeCountry = new Country( 0L, "US", "United States", 0 );

            this.writer.write( Chunk.of( fakeCountry ) );

            int actualCount = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM country", Integer.class );
            assertThat( actualCount ).isEqualTo( 1 );

            var expected = new Country( 0L, "US", "United States", 0 );

            this.jdbcTemplate.query(
                            "SELECT * FROM country WHERE country_code = 0",
                            ( rs, rowNum ) ->
                                    new Country(
                                            rs.getLong( "country_code" ), rs.getString( "abbreviation" ),
                                            rs.getString( "name" ), rs.getLong( "fips_code" )
                                    )
                    )
                    .forEach( country -> assertThat( country ).isEqualTo( expected ));

            return null;
        });

    }

}
