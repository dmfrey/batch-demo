# Batch Demo

## Batch Processing

There is a single `Job` in the demo project, which consists of three, primary, components:

* Reader
* Processors
* Writer

### Reader

A single `Reader` is employeed to read from an input file. A property is used to refer to the file location. This file can be placed in that location by any means.

When Used in coordination with `Spring Cloud Data Flow`, this can be supplied by the Batch definition and overriden at runtime.

### Processors

The processors are the Custom Business Logic used to validate the Business Rule conditions defined below in the breakdown of the data format.

In general, the processors will perform the following:

* Validate the condition to determine the record type
* Validate the domain object doesn't already exist in the database

**NOTE:** Spring Batch utilizes Exceptions and a `SkipPolicy` to determine is a record should be skipped or not.

## Data File

The full data file is located in the project resources: [Country Health Rankings](src/main/resources/sample-data/County_Health_Rankings.csv)

Various Sample files have been extracted from this file and are in the same directory: `src/main/resources/sample-data`

Also, some files have been extracted for the automated tests and are located in `src/test/resources/test-files`

## Data Format

The following sections bread down the file format and various record types within the data.

### Record Format

|            | Column     | Column       | Column     | Column      | Column       | Column       | Column     | Column    | Column      | Column    | Column                          | Column              | Column            | Column   |
|------------|------------|--------------|------------|-------------|--------------|--------------|------------|-----------|-------------|-----------|---------------------------------|---------------------------------|-------------------|----------|
| *Name*     | State      | County       | State code | County code | Year span    | Measure name | Measure id | Numerator | Denominator | Raw value | Confidence Interval Lower Bound | Confidence Interval Upper Bound | Data Release Year | fipscode |
| *Database* | varchar(2) | varchar(100) | bigint     | bigint      | varchar(100) | varchar(100) | bigint     | double | double | double | double | double | varchar(100)      | bigint   |
| *Java* | String | String | long | long | String | String | long | double | double | double | double | double | String | long |

## Record Types

This data can be broken down into various record types. Each will be further defined.

| Record Type | Measure |
|---|---|
| Measure | |
| Country | Country Measure |
| State | State Measure |
| County | County Measure |

### Measure

A `Measure` relates to the type of data being recorded. 

It is only 2 fields:

| Name       | Type   |
|------------|--------|
| Measure Id | long   |
| Name       | String |

### Country

`Country` records can derive both a `Country` domain object and `Country Measure` domain object.

A `Country Measure` requires a dependency on both a `Country` and a `Measure`.

| State | County | State code  | County code  | Year span | Measure name | Measure id  |  Numerator   | Denominator  |  Raw value   | Confidence Interval Lower Bound  | Confidence Interval Upper Bound  | Data Release Year | fipscode  |
| -- | --- |:-----------:|:------------:| --- | --- |:-----------:|:------------:|:------------:|:------------:|:--------------------------------:|:--------------------------------:| --- |:---------:|
| US | United States       |      0      |      0       | 2003-2005    | Violent crime rate |     43      | 1328750.667  |  274877117   | 483.3980657  |                                  |                                  | |     0     |

`Country` records can easily be identified by the following condition:

* State code = 0

**NOTE:** As this data only pertains to the United States, there is only going to be one `Country` domain object ever determined by inspecting the data.

### State

`State` records can derive both a `State` domain object and `State Measure` domain object.

A `State Measure` requires a dependency on both a `Country` and a `Measure`.

| State | County  | State code | County code  | Year span | Measure name | Measure id  |  Numerator   | Denominator  |  Raw value   | Confidence Interval Lower Bound  | Confidence Interval Upper Bound  | Data Release Year | fipscode |
|-------|---------|:----------:|:------------:| --- | --- |:-----------:|:------------:|:------------:|:------------:|:--------------------------------:|:--------------------------------:| --- |:--------:|
| AL    | Alabama |     1      |      0       | 2003-2005    | Violent crime rate |     43      | 18174.83333  |  4221248.167   | 430.5559071  |                                  |                                  | |   1000   |

`State` records can easily be identified by the following conditions (conditions are `and`):

* State code > 0
* County code = 0

### County

`County` records can derive both a `County` domain object and `County Measure` domain object.

`County` domain objects have a dependency on a `State` domain object.

A `County Measure` requires a dependency on both a `County` and a `Measure`.

| State | County  | State code | County code | Year span | Measure name | Measure id  |  Numerator   | Denominator  |  Raw value   | Confidence Interval Lower Bound  | Confidence Interval Upper Bound  | Data Release Year | fipscode |
|-------|---------|:----------:|:-----------:| --- | --- |:-----------:|:------------:|:------------:|:------------:|:--------------------------------:|:--------------------------------:| --- |:--------:|
| AL    | Autauga County |     1      |      1      | 2003-2005    | Violent crime rate |     43      | 141  |  46438.66667   | 303.6262884  |                                  |                                  | |   1001   |

`County` records can easily be identified by the following conditions (conditions are `and`):

* State code > 0
* County code > 0
