package com.broadcom.springconsulting.batch_demo.healthrankings.country;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
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
    public Step countryStep(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                           FlatFileItemReader<InputRow> reader, CountryProcessor processor, ItemWriter<Country> writer ) {

        return new StepBuilder("country step", jobRepository )
                .<InputRow, Country> chunk(100, transactionManager )
                .reader( reader )
                .processor( processor )
                .writer( writer )
                .build();
    }

    @Bean
    CountryStepExecutionListener countryStepExecutionListener( final JdbcTemplate jdbcTemplate ) {

        return new CountryStepExecutionListener( jdbcTemplate );
    }

    @Bean
    CountryProcessor countryProcessor() {

        return new CountryProcessor();
    }

    @Bean
    ItemWriter<Country> countryWriter(DataSource dataSource ) {

        return new JdbcBatchItemWriterBuilder<Country>()
                .sql( "INSERT INTO country (country_code, abbreviation, name, fips_code) VALUES (:countryCode, :abbreviation, :name, :fipsCode) ON CONFLICT (country_code) DO UPDATE set country_code = :countryCode" )
                .dataSource( dataSource )
                .beanMapped()
                .build();
    }

}
