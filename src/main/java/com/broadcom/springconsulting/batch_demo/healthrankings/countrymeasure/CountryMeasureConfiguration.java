package com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure;

import com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure.exception.CountryMeasureSkipPolicy;
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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class CountryMeasureConfiguration {

    @Bean
    Flow countryMeasureFlow( Step countryMeasureStep ) {

        return new FlowBuilder<Flow>("countryMeasureFlow" )
                .start( countryMeasureStep )
                .build();

    }

    @Bean
    public Step countryMeasureStep( JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                                 FlatFileItemReader<InputRow> reader, CountryMeasureProcessor processor, ItemWriter<CountryMeasure> writer ) {

        return new StepBuilder("country measure step", jobRepository )
                .<InputRow, CountryMeasure> chunk(100, transactionManager )
                .reader( reader )
                .processor( processor )
                .writer( writer )
                .faultTolerant()
                .skipPolicy( countryMeasureSkipPolicy() )
                .build();
    }

    @Bean
    CountryMeasureProcessor countryMeasureprocessor() {

        return new CountryMeasureProcessor();
    }

    @Bean
    @StepScope
    JdbcBatchItemWriter<CountryMeasure> countryMeasureWriter( final DataSource dataSource ) {

        return new JdbcBatchItemWriterBuilder<CountryMeasure>()
                .sql( "INSERT INTO country_measure (id, year_span, numerator, denominator, raw_value, confidence_lower_bounds, confidence_upper_bounds, release_year, country_code, measure_id) VALUES (:id, :yearSpan, :numerator, :denominator, :rawValue, :confidenceLowerBounds, :confidenceUpperBounds, :releaseYear, :countryCode, :measureId)" )
                .dataSource( dataSource )
                .beanMapped()
                .build();
    }

    @Bean
    CountryMeasureSkipPolicy countryMeasureSkipPolicy() {

        return new CountryMeasureSkipPolicy();
    }

}
