package com.broadcom.springconsulting.batch_demo.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class JobConfiguration {

    @Bean
    Job importDataJob( JobRepository jobRepository, Flow parallelSteps, Step countyStep, Flow parallelMeasureSteps, JobCompletionNotificationListener listener ) {

        return new JobBuilder( "importDataJob", jobRepository )
                .listener( listener )
                .start( parallelSteps )
                .next( countyStep )
                .next( parallelMeasureSteps )
                .end()
                .build();
    }

    @Bean
    Flow parallelSteps( Flow countryFlow, Flow stateFlow, Flow measureFlow ) {

        return new FlowBuilder<Flow>("parallelSteps" )
                .split( new SimpleAsyncTaskExecutor() )
                .add( countryFlow, stateFlow, measureFlow )
                .build();
    }

    @Bean
    Flow parallelMeasureSteps( Flow countyMeasureFlow ) {

        return new FlowBuilder<Flow>("parallelMeasureSteps" )
                .split( new SimpleAsyncTaskExecutor() )
                .add( countyMeasureFlow )
                .build();
    }

 }
