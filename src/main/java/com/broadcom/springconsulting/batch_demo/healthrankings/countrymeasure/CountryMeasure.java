package com.broadcom.springconsulting.batch_demo.healthrankings.countrymeasure;

import java.util.UUID;

public record CountryMeasure( UUID id, String yearSpan, double numerator, double denominator, double rawValue, double confidenceLowerBounds, double confidenceUpperBounds, String releaseYear, long countryCode, long measureId ) {
}
