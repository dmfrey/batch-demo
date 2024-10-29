package com.broadcom.springconsulting.batch_demo.healthrankings.county;

import com.broadcom.springconsulting.batch_demo.healthrankings.county.client.CountyClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.county.client.CountyClientJdbcClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.county.exception.CountySkipPolicy;
import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
public class CountyConfiguration {

    @Bean
    public Step countyStep(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                           FlatFileItemReader<InputRow> reader, CountyProcessor processor, ItemWriter<County> writer ) {

        return new StepBuilder("county step", jobRepository )
                .<InputRow, County> chunk(100, transactionManager )
                .reader( reader )
                .processor( processor )
                .writer( writer )
                .faultTolerant()
                .skipPolicy( countySkipPolicy() )
                .build();
    }

    @Bean
    CountyStepExecutionListener countyStepExecutionListener( final CountyClient countyClient ) {

        return new CountyStepExecutionListener( countyClient );
    }

    @Bean
    CountyProcessor countyProcessor( final CountyClient countyClient ) {

        return new CountyProcessor( countyClient );
    }

    @Bean
    @StepScope
    JdbcBatchItemWriter<County> countyWriter( final DataSource dataSource ) {

        return new JdbcBatchItemWriterBuilder<County>()
                .sql( "INSERT INTO county (county_code, name, fips_code, state_code) VALUES (:countyCode, :name, :fipsCode, :stateCode) ON CONFLICT (county_code) DO UPDATE set county_code = :countyCode" )
                .dataSource( dataSource )
                .beanMapped()
                .build();
    }

    @Bean
    CountyClient countyClient( final JdbcClient jdbcClient ) {

        return new CountyClientJdbcClient( jdbcClient );
    }

    @Bean
    CountySkipPolicy countySkipPolicy() {

        return new CountySkipPolicy();
    }

}
