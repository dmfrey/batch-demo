package com.broadcom.springconsulting.batch_demo.healthrankings.statemeasure;

import java.util.UUID;

public record StateMeasure( UUID id, String yearSpan, double numerator, double denominator, double rawValue, double confidenceLowerBounds, double confidenceUpperBounds, String releaseYear, long stateCode, long measureId ) {
}
