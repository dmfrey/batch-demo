package com.broadcom.springconsulting.batch_demo.healthrankings.state;

import com.broadcom.springconsulting.batch_demo.TestcontainersConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.exception.NotStateRecordStateProcessorException;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.exception.StateCodeAlreadyExistsStateProcessorException;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import com.broadcom.springconsulting.batch_demo.input.ReaderConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
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
@SpringJUnitConfig( StateConfiguration.class )
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
public class StateConfigurationTests {

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private FlatFileItemReader<InputRow> reader;

    @Autowired
    private StateProcessor processor;

    @Autowired
    private JdbcBatchItemWriter<State> writer;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource( DataSource dataSource ) {

        this.jdbcTemplate = new JdbcTemplate( dataSource );

    }

    @AfterEach
    void cleanUp() {

        jobRepositoryTestUtils.removeJobExecutions();

        this.jdbcTemplate.update( "TRUNCATE TABLE state CASCADE" );

    }

    @Test
    void testReader() {

        assertThat( this.reader ).isNotNull();

    }

    @Test
    void testStateReaderStep() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution( defaultJobParameters( "src/test/resources/test-files/test-state.csv" ) );

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var expected =
                    new InputRow(
                            "AL", "Alabama", 1L, 0L, "2003-2005",
                            "Violent crime rate", 43L, 18174.83333, 4221248.167,
                            430.5559071, null, null, "",
                            1000L
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
    void testStateProcessor_whenCountryInputRecord_verifySkip() throws Exception {

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
                    .isInstanceOf( NotStateRecordStateProcessorException.class );

            return null;
        });

    }

    @Test
    void testStateProcessor_whenCountyInputRecord_verifySkip() throws Exception {

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
                    .isInstanceOf( NotStateRecordStateProcessorException.class );

            return null;
        });

    }

    @Test
    void testStateProcessor_whenStateIdAlreadyExists_verifySkip() throws Exception {

        this.jdbcTemplate.update( "INSERT INTO state (state_code, abbreviation, name, fips_code) VALUES (1, 'AL', 'Alabama', 1000)" );

        var stepExecution = MetaDataInstanceFactory.createStepExecution();

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeInputRow =
                    new InputRow(
                            "AL", "Alabama", 1L, 0L, "2003-2005",
                            "Violent crime rate", 43L, 18174.83333, 4221248.167,
                            430.5559071, null, null, "",
                            1000L
                    );

            assertThatThrownBy( () -> this.processor.process( fakeInputRow ) )
                    .isInstanceOf( StateCodeAlreadyExistsStateProcessorException.class );

            return null;
        });

    }

    @Test
    void testStateProcessorStep() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution();

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeInputRow =
                    new InputRow(
                            "AL", "Alabama", 1L, 0L, "2003-2005",
                            "Violent crime rate", 43L, 18174.83333, 4221248.167,
                            430.5559071, null, null, "",
                            1000L
                    );

            var actual = this.processor.process( fakeInputRow );

            var expected = new State( 1, "AL", "Alabama", 1000 );
            assertThat( actual ).isEqualTo( expected );

            return null;
        });

    }

    @Test
    void testStateWriterStep() throws Exception {

        var stepExecution = MetaDataInstanceFactory.createStepExecution( defaultJobParameters( "src/test/resources/test-files/test-state.csv" ) );

        StepScopeTestUtils.doInStepScope( stepExecution, () -> {

            var fakeState = new State( 1L, "AL", "Alabama", 1000 );

            this.writer.write( Chunk.of( fakeState ) );

            int actualCount = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM state", Integer.class );
            assertThat( actualCount ).isEqualTo( 1 );

            var expected = new State( 1L, "AL", "Alabama", 1000 );

            this.jdbcTemplate.query(
                            "SELECT * FROM state WHERE state_code = 1",
                            ( rs, rowNum ) ->
                                    new State(
                                            rs.getLong( "state_code" ), rs.getString( "abbreviation" ),
                                            rs.getString( "name" ), rs.getLong( "fips_code" )
                                    )
                    )
                    .forEach( state -> {

                        assertThat( state ).isEqualTo( expected );

                    });

            return null;
        });

    }

}
