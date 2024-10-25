package com.broadcom.springconsulting.batch_demo.healthrankings.county;

import com.broadcom.springconsulting.batch_demo.TestcontainersConfiguration;
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

import static com.broadcom.springconsulting.batch_demo.healthrankings.TestUtils.defaultJobParameters;
import static org.assertj.core.api.Assertions.assertThat;

@Import({ TestcontainersConfiguration.class, ReaderConfiguration.class })
@SpringBatchTest
@SpringJUnitConfig( CountyConfiguration.class )
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
public class CountyConfigurationTests {

    private static final Logger log = LoggerFactory.getLogger( CountyConfigurationTests.class );

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private FlatFileItemReader<InputRow> reader;

    @Autowired
    private JdbcBatchItemWriter<County> writer;

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
    void testCountyReaderStep() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution( defaultJobParameters( "src/test/resources/test-files/test-county.csv" ) );

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

    }

    @Test
    void testCountyWriterStep() throws Exception {

        this.jdbcTemplate.update( "INSERT INTO state (state_code, abbreviation, name, fips_code) VALUES (1, 'AL', 'ALABAMA', 1000)" );

        var stepExecution = MetaDataInstanceFactory.createStepExecution( defaultJobParameters( "src/test/resources/test-files/test-county.csv" ) );

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeCounty = new County( 1, "Autauga County", 1001, 1 );

            this.writer.write( Chunk.of( fakeCounty ) );

            int actualCount = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM county", Integer.class );
            assertThat( actualCount ).isEqualTo( 1 );

            var expected = new County( 1, "Autauga County", 1001, 1 );

            this.jdbcTemplate.query(
                            "SELECT * FROM county WHERE county_code = 1",
                            ( rs, rowNum ) ->
                                    new County(
                                            rs.getLong( "county_code" ), rs.getString( "name" ),
                                            rs.getLong( "fips_code" ), rs.getLong( "state_code" )
                                    )
                    )
                    .forEach( county -> assertThat( county ).isEqualTo( expected ) );

            return null;
        });

    }

}
