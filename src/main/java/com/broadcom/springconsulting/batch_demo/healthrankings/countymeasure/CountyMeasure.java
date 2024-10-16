package com.broadcom.springconsulting.batch_demo.healthrankings.countymeasure;

import java.util.UUID;

public record CountyMeasure( UUID id, String yearSpan, double numerator, double denominator, double rawValue, double confidenceLowerBounds, double confidenceUpperBounds, String releaseYear, long countyCode, long measureId ) {
}
