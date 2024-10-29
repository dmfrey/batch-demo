package com.broadcom.springconsulting.batch_demo.healthrankings.state.client;

import com.broadcom.springconsulting.batch_demo.TestcontainersConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.State;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.properties.TestcontainersPropertySourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({ TestcontainersConfiguration.class })
@ImportAutoConfiguration( TestcontainersPropertySourceAutoConfiguration.class )   // Required for DataJpaTests to run with TestcontainersConfiguration
@SpringJUnitConfig( StateClientJdbcClient.class )
@DataJdbcTest
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
@EnableAutoConfiguration
@DirtiesContext
class StateClientTests {

    @Autowired
    StateClient subject;

    final long fakeStateCode = 1;
    final String fakeAbbreviation = "FS";
    final String fakeStateName = "Fake State";
    final long fakeFipsCode = 1;

    @Test
    void testFindAll() {

        this.subject.create( new State( fakeStateCode, fakeAbbreviation, fakeStateName, fakeFipsCode ) );

        var actual = this.subject.findAll();

        var expected = new State( fakeStateCode, fakeAbbreviation, fakeStateName, fakeFipsCode );

        assertThat( actual )
                .containsExactly( expected );
    }

    @Test
    void testFindById() {

        this.subject.create( new State( fakeStateCode, fakeAbbreviation, fakeStateName, fakeFipsCode ) );

        var actual = this.subject.findById( fakeStateCode );

        var expected = new State( fakeStateCode, fakeAbbreviation, fakeStateName, fakeFipsCode );

        assertThat( actual )
                .isPresent()
                .map( country -> country )
                .hasValue( expected );

    }

    @Test
    void testFindById_whenOptionIsNotPresent_verifyNotPresent() {

        var actual = this.subject.findById( fakeStateCode );

        assertThat( actual )
                .isNotPresent();

    }

    @Test
    void testCreate_whenStateExists_verifyDuplicateKeyException() {

        this.subject.create( new State( fakeStateCode, fakeAbbreviation, fakeStateName, fakeFipsCode ) );

        assertThatThrownBy( () -> this.subject.create( new State( fakeStateCode, fakeAbbreviation, fakeStateName, fakeFipsCode ) ) )
            .isInstanceOf( DuplicateKeyException.class );

    }

    @Test
    void testUpdate() {

        var fakeState = new State( fakeStateCode, fakeAbbreviation, fakeStateName, fakeFipsCode );
        this.subject.create( fakeState );

        var fakeUpdatedStateName = fakeStateName + ", updated";
        var updatedState = new State( fakeStateCode, fakeAbbreviation, fakeUpdatedStateName, fakeFipsCode );
        this.subject.update( updatedState, fakeStateCode );

        var actual = this.subject.findById( fakeStateCode );

        var expected = new State( fakeStateCode, fakeAbbreviation, fakeUpdatedStateName, fakeFipsCode );

        assertThat( actual )
                .isPresent()
                .map( country -> country )
                .hasValue( expected );

    }

    @Test
    void testUpdate_whenUpdateFails_verifyIllegalStateException() {

        var fakeUpdatedStateName = fakeStateName + ", updated";
        var updatedState = new State( fakeStateCode, fakeAbbreviation, fakeUpdatedStateName, fakeFipsCode );

        assertThatThrownBy( () -> this.subject.update( updatedState, fakeStateCode ) )
                .isInstanceOf( IllegalStateException.class );

    }

    @Test
    void testDelete() {

        this.subject.create( new State( fakeStateCode, fakeAbbreviation, fakeStateName, fakeFipsCode ) );

        this.subject.delete( fakeStateCode );

        var actual = this.subject.findById( fakeStateCode );

        assertThat( actual )
                .isNotPresent();

    }

    @Test
    void testDelete_whenDeleteFails_verifyIllegalStateException() {

        assertThatThrownBy( () -> this.subject.delete( fakeStateCode ) )
                .isInstanceOf( IllegalStateException.class );

    }

}
