//package com.broadcom.springconsulting.batch_demo;
//
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.context.annotation.Bean;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.utility.DockerImageName;
//
//@TestConfiguration( proxyBeanMethods = false )
//class TestcontainersConfiguration {
//
//	@Bean
//	@ServiceConnection
//	PostgreSQLContainer<?> taskdb() {
//
//		return new PostgreSQLContainer<>( DockerImageName.parse( "postgres:latest" ) );
//	}
//
//	@Bean
//	@ServiceConnection
//	PostgreSQLContainer<?> healthrankingsdb() {
//
//		return new PostgreSQLContainer<>( DockerImageName.parse( "postgres:latest" ) );
//	}
//
//}
