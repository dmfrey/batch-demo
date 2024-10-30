package com.broadcom.springconsulting.batch_demo.healthrankings.county.client;

import com.broadcom.springconsulting.batch_demo.TestcontainersConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.county.County;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.properties.TestcontainersPropertySourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({ TestcontainersConfiguration.class })
@ImportAutoConfiguration( TestcontainersPropertySourceAutoConfiguration.class )   // Required for DataJpaTests to run with TestcontainersConfiguration
@SpringJUnitConfig( CountyClientJdbcClient.class )
@DataJdbcTest
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
@EnableAutoConfiguration
@DirtiesContext
class CountyClientTests {

    @Autowired
    CountyClient subject;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource( DataSource dataSource ) {

        this.jdbcTemplate = new JdbcTemplate( dataSource );

    }

    final long fakeCountyCode = 1;
    final String fakeCountyName = "Fake County";
    final long fakeFipsCode = 1;
    final long fakeStateCode = 1;

    @Test
    void testFindAll() {

        createTestState();
        this.subject.create( new County( fakeCountyCode, fakeCountyName, fakeFipsCode, fakeStateCode ) );

        var actual = this.subject.findAll();

        var expected = new County( fakeCountyCode, fakeCountyName, fakeFipsCode, fakeStateCode );

        assertThat( actual )
                .containsExactly( expected );
    }

    @Test
    void testFindById() {

        createTestState();
        this.subject.create( new County( fakeCountyCode, fakeCountyName, fakeFipsCode, fakeStateCode ) );

        var actual = this.subject.findById( fakeCountyCode );

        var expected = new County( fakeCountyCode, fakeCountyName, fakeFipsCode, fakeStateCode );

        assertThat( actual )
                .isPresent()
                .map( country -> country )
                .hasValue( expected );

    }

    @Test
    void testFindById_whenOptionIsNotPresent_verifyNotPresent() {

        var actual = this.subject.findById( fakeCountyCode );

        assertThat( actual )
                .isNotPresent();

    }

    @Test
    void testCreate_whenCountyExists_verifyDuplicateKeyException() {

        createTestState();
        this.subject.create( new County( fakeCountyCode, fakeCountyName, fakeFipsCode, fakeStateCode ) );

        assertThatThrownBy( () -> this.subject.create( new County( fakeCountyCode, fakeCountyName, fakeFipsCode, fakeStateCode ) ) )
            .isInstanceOf( DuplicateKeyException.class );

    }

    @Test
    void testUpdate() {

        createTestState();

        var fakeCounty = new County( fakeCountyCode, fakeCountyName, fakeFipsCode, fakeStateCode );
        this.subject.create( fakeCounty );

        var fakeUpdatedCountyName = fakeCountyName + ", updated";
        var updatedCounty = new County( fakeCountyCode, fakeUpdatedCountyName, fakeFipsCode, fakeStateCode );
        this.subject.update( updatedCounty, fakeCountyCode );

        var actual = this.subject.findById( fakeCountyCode );

        var expected = new County( fakeCountyCode, fakeUpdatedCountyName, fakeFipsCode, fakeStateCode );

        assertThat( actual )
                .isPresent()
                .map( country -> country )
                .hasValue( expected );

    }

    @Test
    void testUpdate_whenUpdateFails_verifyIllegalStateException() {

        var fakeUpdatedCountyName = fakeCountyName + ", updated";
        var updatedCounty = new County( fakeCountyCode, fakeUpdatedCountyName, fakeFipsCode, fakeStateCode );

        assertThatThrownBy( () -> this.subject.update( updatedCounty, fakeCountyCode ) )
                .isInstanceOf( IllegalStateException.class );

    }

    @Test
    void testDelete() {

        createTestState();

        this.subject.create( new County( fakeCountyCode, fakeCountyName, fakeFipsCode, fakeStateCode ) );

        this.subject.delete( fakeCountyCode );

        var actual = this.subject.findById( fakeCountyCode );

        assertThat( actual )
                .isNotPresent();

    }

    @Test
    void testDelete_whenDeleteFails_verifyIllegalStateException() {

        assertThatThrownBy( () -> this.subject.delete( fakeCountyCode ) )
                .isInstanceOf( IllegalStateException.class );

    }

    @Test
    void testCountByStateCode() {

        createTestState();

        this.subject.create( new County( fakeCountyCode, fakeCountyName, fakeFipsCode, fakeStateCode ) );

        var actual = this.subject.countByStateCode( fakeStateCode );

        assertThat( actual ).isEqualTo( 1 );

    }

    private void createTestState() {

        this.jdbcTemplate.update( "INSERT INTO state (state_code, abbreviation, name, fips_code) VALUES ('" + fakeStateCode + "', 'TS', 'Test STate', 1000)" );

    }

}
