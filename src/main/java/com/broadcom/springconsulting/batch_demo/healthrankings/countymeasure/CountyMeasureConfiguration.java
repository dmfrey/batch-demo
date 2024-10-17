package com.broadcom.springconsulting.batch_demo.healthrankings.countymeasure;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class CountyMeasureConfiguration {

    @Bean
    public Step countyMeasureStep(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                                  FlatFileItemReader<InputRow> reader, CountyMeasureProcessor processor, ItemWriter<CountyMeasure> writer ) {

        return new StepBuilder("county measure step", jobRepository )
                .<InputRow, CountyMeasure> chunk(100, transactionManager )
                .reader( reader )
                .processor( processor )
                .writer( writer )
                .build();
    }

    @Bean
    ItemWriter<CountyMeasure> countyMeasureWriter( @Qualifier( "healthRankingsDataSource" ) DataSource dataSource ) {

        return new JdbcBatchItemWriterBuilder<CountyMeasure>()
                .sql( "INSERT INTO county_measure (id, year_span, numerator, denominator, raw_value, confidence_lower_bounds, confidence_upper_bounds, release_year, county_code, measure_id) VALUES (:id, :yearSpan, :numerator, :denominator, :rawValue, :confidenceLowerBounds, :confidenceUpperBounds, :releaseYear, :countyCode, :measureId)" )
                .dataSource( dataSource )
                .beanMapped()
                .build();
    }

}
