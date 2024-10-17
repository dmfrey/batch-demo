package com.broadcom.springconsulting.batch_demo.healthrankings.state;

import com.broadcom.springconsulting.batch_demo.input.InputRow;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
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
                .build();
    }

    @Bean
    ItemWriter<State> stateWriter( @Qualifier( "healthRankingsDataSource" ) DataSource dataSource ) {

        return new JdbcBatchItemWriterBuilder<State>()
                .sql( "INSERT INTO state (state_code, abbreviation) VALUES (:stateCode, :abbreviation) ON CONFLICT (state_code) DO UPDATE SET state_code = :stateCode" )
                .dataSource( dataSource )
                .beanMapped()
                .build();
    }

}
