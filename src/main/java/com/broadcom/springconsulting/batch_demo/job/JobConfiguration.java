package com.broadcom.springconsulting.batch_demo.job;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class JobConfiguration {

    @Value( "${health-rankings.file.name:classpath:County_Health_Rankings.csv}" )
    private Resource inputResource;

    @Bean
    Job importDataJob( JobRepository jobRepository, Flow parallelSteps, Step countyStep, Step countyMeasureStep, JobCompletionNotificationListener listener ) {

        return new JobBuilder( "importDataJob", jobRepository )
                .listener( listener )
                .start( parallelSteps )
                .next( countyStep )
                .next( countyMeasureStep )
                .end()
                .build();
    }

    @Bean
    Flow parallelSteps( Flow stateFlow, Flow measureFlow ) {

        return new FlowBuilder<Flow>("parallelSteps" )
                .split( new SimpleAsyncTaskExecutor() )
                .add( stateFlow, measureFlow )
                .build();
    }

    @Bean
    FlatFileItemReader<InputRow> reader() {

        return new FlatFileItemReaderBuilder<InputRow>()
                .name( "inputRowItemReader" )
                .resource( inputResource )
                .linesToSkip( 1 )
                .delimited()
                .names( "state", "county", "stateCode", "countyCode", "yearSpan", "measureName", "measureId", "numerator", "denominator", "rawValue", "confidenceIntervalLowerBound", "confidenceIntervalUpperBound", "dataReleaseYear", "fipsCode" )
                .targetType( InputRow.class )
                .build();
    }

 }
