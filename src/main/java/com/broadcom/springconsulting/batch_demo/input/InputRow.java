package com.broadcom.springconsulting.batch_demo.input;

public record InputRow(
        String state,
        String county,
        Long stateCode,
        Long countyCode,
        String yearSpan,
        String measureName,
        Long measureId,
        Double numerator,
        Double denominator,
        Double rawValue,
        Double confidenceIntervalLowerBound,
        Double confidenceIntervalUpperBound,
        String dataReleaseYear,
        Long fipsCode
) {
}
