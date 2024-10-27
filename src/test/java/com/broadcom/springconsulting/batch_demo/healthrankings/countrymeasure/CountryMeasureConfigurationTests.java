package com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure;

import com.broadcom.springconsulting.batch_demo.TestcontainersConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure.exception.CountryCodeRequiredCountryMeasureProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure.exception.MeasureIdRequiredCountryMeasureProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure.exception.NotCountryMeasureRecordCountryMeasureProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import com.broadcom.springconsulting.batch_demo.input.ReaderConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.UUID;

import static com.broadcom.springconsulting.batch_demo.healthrankings.TestUtils.defaultJobParameters;
import static com.broadcom.springconsulting.batch_demo.healthrankings.TestUtils.isType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({ TestcontainersConfiguration.class, ReaderConfiguration.class })
@SpringBatchTest
@SpringJUnitConfig( CountryMeasureConfiguration.class )
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
public class CountryMeasureConfigurationTests {

    private static final Logger log = LoggerFactory.getLogger( CountryMeasureConfigurationTests.class );

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private FlatFileItemReader<InputRow> reader;

    @Autowired
    private CountryMeasureProcessor processor;

    @Autowired
    private JdbcBatchItemWriter<CountryMeasure> writer;

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
    void testCountryMeasureReaderStep() throws Exception {

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
    void testCountryMeasureProcessor_whenCountryCodeIsNull_verifySkip() throws Exception {

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
                    .isInstanceOf( CountryCodeRequiredCountryMeasureProcessorException.class );

            return null;
        });

    }

    @Test
    void testCountryProcessor_whenMeasureIdIsNull_verifySkip() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution();

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeInputRow =
                    new InputRow(
                            "US", "United States", 0L, 0L, "2003-2005",
                            null, null, 1328750.667, 274877117.0,
                            483.3980657, null, null, "",
                            0L
                    );

            assertThatThrownBy( () -> this.processor.process( fakeInputRow ) )
                    .isInstanceOf( MeasureIdRequiredCountryMeasureProcessorException.class );

            return null;
        });

    }

    @Test
    void testCountryMeasureProcessor_whenNotCountryInputRecord_verifySkip() throws Exception {

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
                    .isInstanceOf( NotCountryMeasureRecordCountryMeasureProcessorException.class );

            return null;
        });

    }

    @Test
    void testCountryMeasureProcessorStep() throws Exception {

        var fakeCountryMeasureId = UUID.randomUUID();

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

            var expected = new CountryMeasure( fakeCountryMeasureId, "2003-2005", 1328750.667, 274877117.0, 483.3980657, 0.0, 0.0, "", 0L, 43L );
            assertThat( actual )
                    .usingRecursiveComparison()
                    .withEqualsForFields( isType( UUID.class ), "id" )
                    .isEqualTo( expected );

            return null;
        });

    }

    @Test
    void testCountryMeasureWriterStep() throws Exception {

        this.jdbcTemplate.update( "INSERT INTO country (country_code, abbreviation, name, fips_code) VALUES (0, 'US', 'United States', 0)" );
        this.jdbcTemplate.update( "INSERT INTO measure (measure_id, name) VALUES (43, 'Violent crime rate')" );

        var stepExecution = MetaDataInstanceFactory.createStepExecution( defaultJobParameters( "src/test/resources/test-files/test-state.csv" ) );

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeCountryMeasureId = UUID.randomUUID();
            var fakeCountryMeasure = new CountryMeasure( fakeCountryMeasureId, "2003-2005", 18174.83333, 4221248.167, 430.5559071, 0.0, 0.0, "", 0L, 43L );

            this.writer.write( Chunk.of( fakeCountryMeasure ) );

            int actualCount = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM country_measure", Integer.class );
            assertThat( actualCount ).isEqualTo( 1 );

            var expected = new CountryMeasure( fakeCountryMeasureId, "2003-2005", 18174.83333, 4221248.167, 430.5559071, 0.0, 0.0, "", 0L, 43L );

            this.jdbcTemplate.query(
                            "SELECT * FROM state_measure WHERE state_code = 1 and measure_id = 43",
                            ( rs, rowNum ) ->
                                    new CountryMeasure(
                                            rs.getObject( "id", UUID.class ), rs.getString( "year_span" ),
                                            rs.getDouble( "numerator" ), rs.getDouble( "denominator" ), rs.getDouble( "raw_value" ),
                                            rs.getDouble( "confidence_lower_bounds" ), rs.getDouble( "confidence_upper_bounds" ), rs.getString( "release_year" ),
                                            rs.getLong( "state_code" ), rs.getLong( "measure_id" )
                                    )
                    )
                    .forEach( stateMeasure -> {

                        log.debug( "CountryMeasure: {}", stateMeasure );

                        assertThat( stateMeasure )
                                .usingRecursiveComparison()
                                .withEqualsForFields( isType( UUID.class ), "id" )
                                .isEqualTo( expected );

                    });

            return null;
        });

    }

}
