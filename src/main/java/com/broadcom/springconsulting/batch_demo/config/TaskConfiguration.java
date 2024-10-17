package com.broadcom.springconsulting.batch_demo.config;

import org.springframework.cloud.task.configuration.DefaultTaskConfigurer;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableTask
//@ConditionalOnBean( SpringLiquibase.class )
public class TaskConfiguration {

    @Bean
    CustomTaskConfigurer customTaskConfigurer( DataSource dataSource ) {

        return new CustomTaskConfigurer( dataSource );
    }

    static class CustomTaskConfigurer extends DefaultTaskConfigurer {

        CustomTaskConfigurer( DataSource taskDataSource) {
            super( taskDataSource );
        }

    }

}
