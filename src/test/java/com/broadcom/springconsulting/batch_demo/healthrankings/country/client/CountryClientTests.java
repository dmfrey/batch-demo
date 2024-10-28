package com.broadcom.springconsulting.batch_demo.healthrankings.country.client;

import com.broadcom.springconsulting.batch_demo.TestcontainersConfiguration;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.Country;
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
@SpringJUnitConfig( CountryClientJdbcClient.class )
@DataJdbcTest
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
@EnableAutoConfiguration
@DirtiesContext
class CountryClientTests {

    @Autowired
    CountryClient subject;

    final long fakeCountryCode = 1;
    final String fakeCountryAbbreviation = "FC";
    final String fakeCountryName = "Fake Country";
    final long fakeFipsCode = 1;

    @Test
    void testFindAll() {

        this.subject.create( new Country( fakeCountryCode, fakeCountryAbbreviation, fakeCountryName, fakeFipsCode ) );

        var actual = this.subject.findAll();

        var expected = new Country( fakeCountryCode, fakeCountryAbbreviation, fakeCountryName, fakeFipsCode );

        assertThat( actual )
                .containsExactly( expected );
    }

    @Test
    void testFindById() {

        this.subject.create( new Country( fakeCountryCode, fakeCountryAbbreviation, fakeCountryName, fakeFipsCode ) );

        var actual = this.subject.findById( fakeCountryCode );

        var expected = new Country( fakeCountryCode, fakeCountryAbbreviation, fakeCountryName, fakeFipsCode );

        assertThat( actual )
                .isPresent()
                .map( country -> country )
                .hasValue( expected );

    }

    @Test
    void testFindById_whenOptionIsNotPresent_verifyNotPresent() {

        var actual = this.subject.findById( fakeCountryCode );

        assertThat( actual )
                .isNotPresent();

    }

    @Test
    void testCreate_whenCountryExists_verifyDuplicateKeyException() {

        this.subject.create( new Country( fakeCountryCode, fakeCountryAbbreviation, fakeCountryName, fakeFipsCode ) );

        assertThatThrownBy( () -> this.subject.create( new Country( fakeCountryCode, fakeCountryAbbreviation, fakeCountryName, fakeFipsCode ) ) )
            .isInstanceOf( DuplicateKeyException.class );

    }

    @Test
    void testUpdate() {

        var fakeCountry = new Country( fakeCountryCode, fakeCountryAbbreviation, fakeCountryName, fakeFipsCode );
        this.subject.create( fakeCountry );

        var fakeUpdatedCountryName = fakeCountryName + ", updated";
        var updatedCountry = new Country( fakeCountryCode, fakeCountryAbbreviation, fakeUpdatedCountryName, fakeFipsCode );
        this.subject.update( updatedCountry, fakeCountryCode );

        var actual = this.subject.findById( fakeCountryCode );

        var expected = new Country( fakeCountryCode, fakeCountryAbbreviation, fakeUpdatedCountryName, fakeFipsCode );

        assertThat( actual )
                .isPresent()
                .map( country -> country )
                .hasValue( expected );

    }

    @Test
    void testUpdate_whenUpdateFails_verifyIllegalStateException() {

        var fakeUpdatedCountryName = fakeCountryName + ", updated";
        var updatedCountry = new Country( fakeCountryCode, fakeCountryAbbreviation, fakeUpdatedCountryName, fakeFipsCode );

        assertThatThrownBy( () -> this.subject.update( updatedCountry, fakeCountryCode ) )
                .isInstanceOf( IllegalStateException.class );

    }

    @Test
    void testDelete() {

        this.subject.create( new Country( fakeCountryCode, fakeCountryAbbreviation, fakeCountryName, fakeFipsCode ) );

        this.subject.delete( fakeCountryCode );

        var actual = this.subject.findById( fakeCountryCode );

        assertThat( actual )
                .isNotPresent();

    }

    @Test
    void testDelete_whenDeleteFails_verifyIllegalStateException() {

        assertThatThrownBy( () -> this.subject.delete( fakeCountryCode ) )
                .isInstanceOf( IllegalStateException.class );

    }

}
