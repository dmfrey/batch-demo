package com.broadcom.springconsulting.batch_demo.healthrankings.country;

import com.broadcom.springconsulting.batch_demo.healthrankings.country.client.CountryClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.client.CountryClientJdbcClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.country.exception.CountrySkipPolicy;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class CountryConfiguration {

    @Bean
    Flow countryFlow( Step countryStep ) {

        return new FlowBuilder<Flow>("countryFlow" )
                .start( countryStep )
                .build();

    }

    @Bean
    public Step countryStep( JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                           FlatFileItemReader<InputRow> reader, CountryProcessor processor, ItemWriter<Country> writer ) {

        return new StepBuilder("country step", jobRepository )
                .<InputRow, Country> chunk(5, transactionManager )
                .reader( reader )
                .processor( processor )
                .writer( writer )
                .faultTolerant()
                .skipPolicy( countrySkipPolicy() )
                .build();
    }

    @Bean
    CountryStepExecutionListener countryStepExecutionListener( final CountryClient countryClient ) {

        return new CountryStepExecutionListener( countryClient );
    }

    @Bean
    CountryProcessor countryProcessor( final CountryClient countryClient ) {

        return new CountryProcessor( countryClient );
    }

    @Bean
    @StepScope
    JdbcBatchItemWriter<Country> countryWriter( final DataSource dataSource ) {

        return new JdbcBatchItemWriterBuilder<Country>()
                .sql( "INSERT INTO country (country_code, abbreviation, name, fips_code) VALUES (:countryCode, :abbreviation, :name, :fipsCode) ON CONFLICT (country_code) DO UPDATE set country_code = :countryCode" )
                .dataSource( dataSource )
                .beanMapped()
                .build();
    }

    @Bean
    CountryClient countryClient( final JdbcClient jdbcClient ) {

        return new CountryClientJdbcClient( jdbcClient );
    }

    @Bean
    CountrySkipPolicy countrySkipPolicy() {

        return new CountrySkipPolicy();
    }

}
