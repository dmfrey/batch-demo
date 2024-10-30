package com.broadcom.springconsulting.batch_demo.healthrankings.state;

import com.broadcom.springconsulting.batch_demo.healthrankings.state.client.StateClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.client.StateClientJdbcClient;
import com.broadcom.springconsulting.batch_demo.healthrankings.state.exception.StateSkipPolicy;
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
public class StateConfiguration {

    @Bean
    Flow stateFlow( Step stateStep ) {

        return new FlowBuilder<Flow>("stateFlow" )
                .start( stateStep )
                .build();

    }

    @Bean
    public Step stateStep( JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                           FlatFileItemReader<InputRow> reader, StateProcessor processor, ItemWriter<State> writer,
                           StateStepExecutionListener listener ) {

        return new StepBuilder("state step", jobRepository )
                .<InputRow, State> chunk(100, transactionManager )
                .listener( listener )
                .reader( reader )
                .processor( processor )
                .writer( writer )
                .faultTolerant()
                .skipPolicy( stateSkipPolicy() )
                .build();
    }

    @Bean
    StateStepExecutionListener stateStepExecutionListener( final StateClient stateClient ) {

        return new StateStepExecutionListener( stateClient );
    }

    @Bean
    StateProcessor stateProcessor( final StateClient stateClient ) {

        return new StateProcessor( stateClient );
    }

    @Bean
    @StepScope
    JdbcBatchItemWriter<State> stateWriter( final DataSource dataSource ) {

        return new JdbcBatchItemWriterBuilder<State>()
                .sql( "INSERT INTO state (state_code, abbreviation, name, fips_code) VALUES (:stateCode, :abbreviation, :name, :fipsCode) ON CONFLICT (state_code) DO UPDATE SET state_code = :stateCode" )
                .dataSource( dataSource )
                .beanMapped()
                .build();
    }

    @Bean
    StateClient stateClient( final JdbcClient jdbcClient ) {

        return new StateClientJdbcClient( jdbcClient );
    }

    @Bean
    StateSkipPolicy stateSkipPolicy() {

        return new StateSkipPolicy();
    }

}
