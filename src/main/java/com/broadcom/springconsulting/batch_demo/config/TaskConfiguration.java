package com.broadcom.springconsulting.batch_demo.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean( SpringLiquibase.class )
@EnableTask
public class TaskConfiguration {

}
