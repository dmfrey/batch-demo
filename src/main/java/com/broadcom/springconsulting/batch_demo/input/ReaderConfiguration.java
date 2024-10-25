package com.broadcom.springconsulting.batch_demo.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;

@Configuration
public class ReaderConfiguration {

    private static final Logger log = LoggerFactory.getLogger( ReaderConfiguration.class );

    @Bean
    @StepScope
    FlatFileItemReader<InputRow> reader(@Value( "#{jobParameters['localFilePath'] ?: 'src/main/resources/sample-data/test.csv'}" ) String filePath ) {

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
