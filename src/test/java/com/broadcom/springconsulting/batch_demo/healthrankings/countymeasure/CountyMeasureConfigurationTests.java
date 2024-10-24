package com.broadcom.springconsulting.batch_demo.healthrankings.countymeasure;

import com.broadcom.springconsulting.batch_demo.TestcontainersConfiguration;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import com.broadcom.springconsulting.batch_demo.input.ReaderConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
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
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.sql.DataSource;
import java.util.UUID;
import java.util.function.BiPredicate;

import static org.assertj.core.api.Assertions.assertThat;

@Import({ TestcontainersConfiguration.class, ReaderConfiguration.class })
@SpringBatchTest
@SpringJUnitConfig( CountyMeasureConfiguration.class )
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
public class CountyMeasureConfigurationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private FlatFileItemReader<InputRow> reader;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource( DataSource dataSource ) {

        this.jdbcTemplate = new JdbcTemplate( dataSource );

    }

    @AfterEach
    void cleanUp() {

        jobRepositoryTestUtils.removeJobExecutions();

    }

    @Test
    void testReader() {

        assertThat( this.reader ).isNotNull();

    }

    @Test
    void testCountyMeasureStep() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution( defaultJobParameters() );

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var expected =
                    new InputRow(
                            "AL", "Autauga County", 1L, 1L, "2003-2005",
                            "Violent crime rate", 43L, 141.0, 46438.66667,
                            303.6262884, null, null, "",
                            1001L
                    );

            this.reader.open( stepExecution.getExecutionContext() );

            InputRow inputRow;
            while( ( inputRow = this.reader.read() ) != null ) {
                assertThat( inputRow ).isEqualTo( expected );
            }
            this.reader.close();

            return null;
        });

        var expected = new CountyMeasure( null, "2003-2005", 141.0, 46438.66667, 303.6262884, 0.0, 0.0, "", 1L, 43L );

        this.jdbcTemplate.query(
                "SELECT * FROM county_measure WHERE county_code = 1 and measure_id = 43",
                        ( rs, rowNum ) ->
                                new CountyMeasure(
                                        rs.getObject( "id", UUID.class ), rs.getString( "year_span" ),
                                        rs.getDouble( "numerator" ), rs.getDouble( "denominator" ), rs.getDouble( "raw_value" ),
                                        rs.getDouble( "confidence_lower_bounds" ), rs.getDouble( "confidence_upper_bounds" ), rs.getString( "release_year" ),
                                        rs.getLong( "county_code" ), rs.getLong( "measure_id" )
                                )
                )
                .forEach( countryMeasure ->
                        assertThat( countryMeasure )
                                .usingRecursiveComparison()
                                .withEqualsForFields( isType( UUID.class ), "id" )
                                .isEqualTo( expected ) );

    }

    private JobParameters defaultJobParameters() {

        var paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString( "localFilePath", "src/test/resources/test-files/test-county.csv" );

        return paramsBuilder.toJobParameters();
    }

    <A, B, T extends Class<?>> BiPredicate<A, B> isType(T type) {
        return (a, b) -> type.isInstance(a) && type.isInstance(b);
    }

}
