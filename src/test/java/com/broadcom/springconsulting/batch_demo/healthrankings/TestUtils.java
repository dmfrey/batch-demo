package com.broadcom.springconsulting.batch_demo.healthrankings;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.util.function.BiPredicate;

public final class TestUtils {

    public static <A, B, T extends Class<?>> BiPredicate<A, B> isType( T type ) {

        return (a, b) -> type.isInstance(a) && type.isInstance(b);
    }

    public static JobParameters defaultJobParameters( String filePath ) {

        var paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString( "localFilePath", filePath );

        return paramsBuilder.toJobParameters();
    }

}
