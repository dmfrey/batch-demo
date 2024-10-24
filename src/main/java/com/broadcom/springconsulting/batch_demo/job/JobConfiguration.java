package com.broadcom.springconsulting.batch_demo.job;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class JobConfiguration {

    private static final Logger log = LoggerFactory.getLogger( JobConfiguration.class );

    private final ResourceLoader resourceLoader;

    public JobConfiguration( final ResourceLoader resourceLoader ) {

        this.resourceLoader = resourceLoader;

    }

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
    @StepScope
    FlatFileItemReader<InputRow> reader( @Value( "#{jobParameters['localFilePath'] ?: 'src/main/resources/sample-data/test.csv'}" ) String filePath ) {

        log.info( "reader : processing file [{}]", filePath );

        return new FlatFileItemReaderBuilder<InputRow>()
                .name( "inputRowItemReader" )
                .resource( new PathResource( filePath ) )
                .linesToSkip( 1 )
                .delimited()
                .names( "state", "county", "stateCode", "countyCode", "yearSpan", "measureName", "measureId", "numerator", "denominator", "rawValue", "confidenceIntervalLowerBound", "confidenceIntervalUpperBound", "dataReleaseYear", "fipsCode" )
                .targetType( InputRow.class )
                .build();
    }

 }
