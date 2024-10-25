package com.broadcom.springconsulting.batch_demo.healthrankings.measure;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MeasureConfiguration {

    @Bean
    Flow measureFlow( Step measureStep ) {

        return new FlowBuilder<Flow>("measureFlow" )
                .start( measureStep )
                .build();

    }

    @Bean
    public Step measureStep( JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                            FlatFileItemReader<InputRow> reader, MeasureProcessor processor, ItemWriter<Measure> writer,
                             MeasureStepExecutionListener listener ) {

        return new StepBuilder( "measure step", jobRepository )
                .<InputRow, Measure> chunk(100, transactionManager )
                .listener( listener )
                .reader( reader )
                .processor( processor )
                .writer( writer )
                .build();
    }

    @Bean
    MeasureStepExecutionListener measureStepExecutionListener( final JdbcTemplate jdbcTemplate ) {

        return new MeasureStepExecutionListener( jdbcTemplate );
    }

    @Bean
    MeasureProcessor measureProcessor() {

        return new MeasureProcessor();
    }

    @Bean
    @StepScope
    JdbcBatchItemWriter<Measure> measureWriter( final DataSource dataSource ) {

        return new JdbcBatchItemWriterBuilder<Measure>()
                .sql( "INSERT INTO measure (measure_id, name) VALUES (:measureId, :name) ON CONFLICT (measure_id) DO UPDATE set measure_id = :measureId" )
                .dataSource( dataSource )
                .beanMapped()
                .build();
    }

}
