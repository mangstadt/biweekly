# biweekly

|     |     |
| --- | --- |
| Continuous Integration: | [![](https://travis-ci.org/mangstadt/biweekly.svg?branch=master)](https://travis-ci.org/mangstadt/biweekly) |
| Code Coverage: | [![codecov.io](http://codecov.io/github/mangstadt/biweekly/coverage.svg?branch=master)](http://codecov.io/github/mangstadt/biweekly?branch=master) |
| Maven Central: | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.sf.biweekly/biweekly/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.sf.biweekly/biweekly) |
| Chat Room: | [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/mangstadt/biweekly?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) |
| License: | [![FreeBSD License](https://img.shields.io/badge/License-FreeBSD-red.svg)](https://github.com/mangstadt/biweekly/blob/master/LICENSE) |

biweekly is an iCalendar library written in Java. The project aims to provide a well documented, easy to use API for reading and writing iCalendar data.

<p align="center"><strong><a href="https://github.com/mangstadt/biweekly/wiki/Downloads">Downloads</a> |
<a href="http://mangstadt.github.io/biweekly/javadocs/latest/index.html">Javadocs</a> |
<a href="#mavengradle">Maven/Gradle</a> | <a href="https://github.com/mangstadt/biweekly/wiki">Documentation</a></strong></p>

# Code sample

## Reading an iCal

```java
String str =
"BEGIN:VCALENDAR\r\n" +
  "VERSION:2.0\r\n" +
  "PRODID:-//Microsoft Corporation//Outlook 14.0 MIMEDIR//EN\r\n" +
  "BEGIN:VEVENT\r\n" +
    "UID:0123\r\n" +
    "DTSTAMP:20130601T080000Z\r\n" +
    "SUMMARY;LANGUAGE=en-us:Team Meeting\r\n" +
    "DTSTART:20130610T120000Z\r\n" +
    "DURATION:PT1H\r\n" +
    "RRULE:FREQ=WEEKLY;INTERVAL=2\r\n" +
  "END:VEVENT\r\n" +
"END:VCALENDAR\r\n";

ICalendar ical = Biweekly.parse(str).first();

VEvent event = ical.getEvents().get(0);
String summary = event.getSummary().getValue();
```

## Writing an iCal

```java
ICalendar ical = new ICalendar();
  VEvent event = new VEvent();
    Summary summary = event.setSummary("Team Meeting");
    summary.setLanguage("en-us");

    Date start = ...
    event.setDateStart(start);

    Duration duration = new Duration.Builder().hours(1).build();
    event.setDuration(duration);

    Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).interval(2).build();
    event.setRecurrenceRule(recur);
  ical.addEvent(event);

String str = Biweekly.write(ical).go();
```

# Features

 * Simple, intuitive API (see [Examples](https://github.com/mangstadt/biweekly/wiki/Examples)).
 * Android compatibility.
 * Full compliance with iCalendar and vCalendar specifications (see [Supported Specifications](https://github.com/mangstadt/biweekly/wiki/Supported-Specifications)).
 * Supports XML and JSON formats (see [Supported Specifications](https://github.com/mangstadt/biweekly/wiki/Supported-Specifications)).
 * Full timezone support (uses [tzurl.org](http://tzurl.org) for timezone definitions).
 * Extensive unit test coverage.
 * Low Java version requirement (1.6 or above).
 * Few dependencies on external libraries.  Dependencies can be selectively excluded based on the functionality that is needed (see [Dependencies](https://github.com/mangstadt/biweekly/wiki/Dependencies)).

# Maven/Gradle

**Maven**

```xml
<dependency>
   <groupId>net.sf.biweekly</groupId>
   <artifactId>biweekly</artifactId>
   <version>0.6.6</version>
</dependency>
```

**Gradle**

```
implementation 'net.sf.biweekly:biweekly:0.6.6'
// or use the `api` keyword if you are exposing parts of biweekly in your API
```

# Build Instructions

biweekly uses [Maven](http://maven.apache.org/) as its build tool, and adheres to its convensions.

To build the project: `mvn compile`  
To run the unit tests: `mvn test`  
To build a JAR: `mvn package`

# Questions / Feedback

You have some options:

 * [Issue tracker](https://github.com/mangstadt/biweekly/issues)
 * [Gitter chat room](https://gitter.im/mangstadt/biweekly)
 * [Post a question to StackOverflow](http://stackoverflow.com/questions/ask) with `ical` as a tag
 * Email me directly: [mike.angstadt@gmail.com](mailto:mike.angstadt@gmail.com)

Please submit bug reports and feature requests to the [issue tracker](https://github.com/mangstadt/biweekly/issues).

[![](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=8CEN7MPKRBKU6&lc=US&item_name=Michael%20Angstadt&item_number=biweekly&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)

*This project was born on June 23, 2013 on [Sourceforge](http://sf.net/p/biweekly).  It migrated to Github on November 22, 2015.*
