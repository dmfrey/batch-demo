package com.broadcom.springconsulting.batch_demo.healthrankings.statemeasure;

import com.broadcom.springconsulting.batch_demo.healthrankings.statemeasure.exception.StateMeasureSkipPolicy;
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
public class StateMeasureConfiguration {

    @Bean
    Flow stateMeasureFlow( Step stateMeasureStep ) {

        return new FlowBuilder<Flow>("stateMeasureFlow" )
                .start( stateMeasureStep )
                .build();

    }

    @Bean
    public Step stateMeasureStep( JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                                  FlatFileItemReader<InputRow> reader, StateMeasureProcessor processor, ItemWriter<StateMeasure> writer ) {

        return new StepBuilder("state measure step", jobRepository )
                .<InputRow, StateMeasure> chunk(100, transactionManager )
                .reader( reader )
                .processor( processor )
                .writer( writer )
                .faultTolerant()
                .skipPolicy( stateMeasureSkipPolicy() )
                .build();
    }

    @Bean
    StateMeasureProcessor stateMeasureProcessor() {

        return new StateMeasureProcessor();
    }

    @Bean
    @StepScope
    JdbcBatchItemWriter<StateMeasure> stateMeasureWriter( final DataSource dataSource ) {

        return new JdbcBatchItemWriterBuilder<StateMeasure>()
                .sql( "INSERT INTO state_measure (id, year_span, numerator, denominator, raw_value, confidence_lower_bounds, confidence_upper_bounds, release_year, state_code, measure_id) VALUES (:id, :yearSpan, :numerator, :denominator, :rawValue, :confidenceLowerBounds, :confidenceUpperBounds, :releaseYear, :stateCode, :measureId)" )
                .dataSource( dataSource )
                .beanMapped()
                .build();
    }

    @Bean
    StateMeasureSkipPolicy stateMeasureSkipPolicy() {

        return new StateMeasureSkipPolicy();
    }

}
