package biweekly.io.scribe.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Rule;
import org.junit.Test;

import biweekly.ICalVersion;
import biweekly.component.StandardTime;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext;
import biweekly.io.ParseContext.TimezonedDate;
import biweekly.io.TimezoneInfo;
import biweekly.io.json.JCalValue;
import biweekly.io.json.JsonValue;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.RecurrenceProperty;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.ICalDate;
import biweekly.util.ListMultimap;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.ByDay;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;

/*
 Copyright (c) 2013-2016, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @author Michael Angstadt
 */
public class RecurrencePropertyScribeTest extends ScribeTest<RecurrenceProperty> {
	@Rule
	public final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(2, 0);

	private final ICalDate date = new ICalDate(date("2013-06-11"), false);
	private final String dateStr = "20130611";
	private final String dateStrExt = "2013-06-11";

	private final ICalDate datetime = new ICalDate(utc("2013-06-11 12:43:02"));
	private final String dateTimeStr = dateStr + "T124302Z";
	private final String dateTimeStrExt = dateStrExt + "T12:43:02Z";

	private final RecurrenceProperty withMultiple;
	{
		//@formatter:off
		Recurrence.Builder builder = new Recurrence.Builder(Frequency.WEEKLY)
		.byYearDay(100)
		.byYearDay(101)
		.byMonthDay(1)
		.byMonthDay(2)
		.byMonth(5)
		.byMonth(6)
		.byHour(1)
		.byHour(2)
		.byMinute(3)
		.byMinute(4)
		.bySecond(58)
		.bySecond(59)
		.bySetPos(7)
		.bySetPos(8)
		.bySetPos(9)
		.byWeekNo(1)
		.byWeekNo(2)
		.count(5)
		.interval(10)
		.workweekStarts(DayOfWeek.TUESDAY)
		.xrule("X-NAME", "one")
		.xrule("x-name", "two") //converts name to uppercase
		.xrule("X-RULE", "three");
		//@formatter:on

		for (DayOfWeek dow : DayOfWeek.values()) {
			builder.byDay(dow);
		}
		builder.byDay(5, DayOfWeek.FRIDAY);

		withMultiple = new RecurrenceProperty(builder.build());
	}
	private final RecurrenceProperty withSingle;
	{
		//@formatter:off
		Recurrence.Builder builder = new Recurrence.Builder(Frequency.WEEKLY)
		.byYearDay(100)
		.byMonthDay(1)
		.byMonth(5)
		.byHour(1)
		.byMinute(3)
		.bySecond(58)
		.bySetPos(7)
		.byWeekNo(1)
		.count(5)
		.interval(10)
		.byDay(DayOfWeek.FRIDAY)
		.workweekStarts(DayOfWeek.TUESDAY)
		.xrule("X-NAME", "one")
		.xrule("X-RULE", "three");
		//@formatter:on

		withSingle = new RecurrenceProperty(builder.build());
	}
	private final RecurrenceProperty withUntilDate = new RecurrenceProperty(new Recurrence.Builder(Frequency.WEEKLY).until(date).build());
	private final RecurrenceProperty withUntilDateTime = new RecurrenceProperty(new Recurrence.Builder(Frequency.WEEKLY).until(datetime).build());
	private final RecurrenceProperty empty = new RecurrenceProperty((Recurrence) null);

	public RecurrencePropertyScribeTest() {
		super(new RecurrencePropertyMarshallerImpl());
	}

	@Test
	public void writeText_vcal() throws Exception {
		RecurrenceProperty prop = new RecurrenceProperty((Recurrence) null);
		String expected, actual;

		prop.setValue(null);
		expected = "";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder((Frequency) null).build());
		expected = "";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.SECONDLY).build());
		expected = "";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.MINUTELY).build());
		expected = "M1 #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.MINUTELY).interval(5).count(10).build());
		expected = "M5 #10";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.MINUTELY).interval(5).until(new ICalDate(utc("2000-01-01 01:00:00"))).build());
		expected = "M5 20000101T010000Z";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.MINUTELY).interval(5).until(new ICalDate(utc("2000-01-01 12:00:00"))).build());
		expected = "M5 20000101T070000";
		actual = sensei.assertWriteText(prop).version(V1_0).tz(americaNewYork()).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.HOURLY).interval(2).build());
		expected = "M120 #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.DAILY).interval(2).build());
		expected = "D2 #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.WEEKLY).interval(2).byDay(DayOfWeek.MONDAY).byDay(DayOfWeek.WEDNESDAY).build());
		expected = "W2 MO WE #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.WEEKLY).interval(2).build());
		expected = "W2 #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.WEEKLY).interval(2).byDay(DayOfWeek.MONDAY).byDay(DayOfWeek.WEDNESDAY).build());
		expected = "W2 MO WE #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.MONTHLY).interval(2).build());
		expected = "MP2 #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.MONTHLY).interval(2).byDay(1, DayOfWeek.MONDAY).byDay(-1, DayOfWeek.WEDNESDAY).build());
		expected = "MP2 1+ MO 1- WE #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.MONTHLY).interval(2).byMonthDay(10).byMonthDay(-2).build());
		expected = "MD2 10+ 2- #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.YEARLY).interval(2).build());
		expected = "YD2 #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.YEARLY).interval(2).byMonth(2).byMonth(3).build());
		expected = "YM2 2 3 #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);

		prop.setValue(new Recurrence.Builder(Frequency.YEARLY).interval(2).byYearDay(2).byYearDay(100).build());
		expected = "YD2 2 100 #0";
		actual = sensei.assertWriteText(prop).version(V1_0).run();
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_multiples() {
		String actual = sensei.assertWriteText(withMultiple).run();
		List<String> split = Arrays.asList(actual.split(";"));

		assertEquals(15, split.size());
		assertEquals("FREQ=WEEKLY", split.get(0));
		assertTrue(actual, split.contains("BYYEARDAY=100,101"));
		assertTrue(actual, split.contains("BYMONTHDAY=1,2"));
		assertTrue(actual, split.contains("BYMONTH=5,6"));
		assertTrue(actual, split.contains("BYHOUR=1,2"));
		assertTrue(actual, split.contains("BYMINUTE=3,4"));
		assertTrue(actual, split.contains("BYSECOND=58,59"));
		assertTrue(actual, split.contains("BYSETPOS=7,8,9"));
		assertTrue(actual, split.contains("BYWEEKNO=1,2"));
		assertTrue(actual, split.contains("COUNT=5"));
		assertTrue(actual, split.contains("INTERVAL=10"));
		assertTrue(actual, split.contains("BYDAY=MO,TU,WE,TH,FR,SA,SU,5FR"));
		assertTrue(actual, split.contains("WKST=TU"));
		assertTrue(actual, split.contains("X-NAME=one,two"));
		assertTrue(actual, split.contains("X-RULE=three"));
	}

	@Test
	public void writeText_singles() {
		String actual = sensei.assertWriteText(withSingle).run();
		List<String> split = Arrays.asList(actual.split(";"));

		assertEquals(15, split.size());
		assertEquals("FREQ=WEEKLY", split.get(0));
		assertTrue(actual, split.contains("BYYEARDAY=100"));
		assertTrue(actual, split.contains("BYMONTHDAY=1"));
		assertTrue(actual, split.contains("BYMONTH=5"));
		assertTrue(actual, split.contains("BYHOUR=1"));
		assertTrue(actual, split.contains("BYMINUTE=3"));
		assertTrue(actual, split.contains("BYSECOND=58"));
		assertTrue(actual, split.contains("BYSETPOS=7"));
		assertTrue(actual, split.contains("BYWEEKNO=1"));
		assertTrue(actual, split.contains("COUNT=5"));
		assertTrue(actual, split.contains("INTERVAL=10"));
		assertTrue(actual, split.contains("BYDAY=FR"));
		assertTrue(actual, split.contains("WKST=TU"));
		assertTrue(actual, split.contains("X-NAME=one"));
		assertTrue(actual, split.contains("X-RULE=three"));
	}

	@Test
	public void writeText_until_datetime() {
		String utc = "FREQ=WEEKLY;UNTIL=20130611T124302Z";
		String floating = "FREQ=WEEKLY;UNTIL=20130611T144302";

		//no DTSTART sibling
		sensei.assertWriteText(withUntilDateTime).run(utc);
		sensei.assertWriteText(withUntilDateTime).tz(americaNewYork()).run(utc);
		sensei.assertWriteText(withUntilDateTime).tz(floating()).run(utc);

		VEvent event = new VEvent();
		event.addProperty(withUntilDateTime);
		event.setDateStart(datetime);
		sensei.assertWriteText(withUntilDateTime).parent(event).run(utc);
		sensei.assertWriteText(withUntilDateTime).parent(event).tz(americaNewYork()).run(utc);
		sensei.assertWriteText(withUntilDateTime).parent(event).tz(floating()).run(floating);
		sensei.assertWriteText(withUntilDateTime).version(ICalVersion.V2_0_DEPRECATED).parent(event).tz(floating()).run(utc);

		StandardTime standard = new StandardTime();
		standard.addProperty(withUntilDateTime);
		sensei.assertWriteText(withUntilDateTime).parent(standard).run(utc);
		sensei.assertWriteText(withUntilDateTime).parent(standard).tz(americaNewYork()).run(utc);
		sensei.assertWriteText(withUntilDateTime).parent(standard).tz(floating()).run(utc);
	}

	@Test
	public void writeText_until_date() {
		String expected = "FREQ=WEEKLY;UNTIL=" + dateStr;

		//no DTSTART sibling
		sensei.assertWriteText(withUntilDate).run(expected);
		sensei.assertWriteText(withUntilDate).tz(americaNewYork()).run(expected);
		sensei.assertWriteText(withUntilDate).tz(floating()).run(expected);

		VEvent event = new VEvent();
		event.addProperty(withUntilDate);
		event.setDateStart(datetime);
		sensei.assertWriteText(withUntilDate).parent(event).run(expected);
		sensei.assertWriteText(withUntilDate).parent(event).tz(americaNewYork()).run(expected);
		sensei.assertWriteText(withUntilDate).parent(event).tz(floating()).run(expected);
		sensei.assertWriteText(withUntilDate).version(ICalVersion.V2_0_DEPRECATED).parent(event).tz(floating()).run(expected);

		StandardTime standard = new StandardTime();
		standard.addProperty(withUntilDate);
		standard.setDateStart(datetime);
		sensei.assertWriteText(withUntilDate).parent(standard).run(expected);
		sensei.assertWriteText(withUntilDate).parent(standard).tz(americaNewYork()).run(expected);
		sensei.assertWriteText(withUntilDate).parent(standard).tz(floating()).run(expected);
	}

	@Test
	public void writeText_empty() {
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText_vcal() throws Exception {
		sensei.assertParseText("S2").versions(V1_0).cannotParse();

		sensei.assertParseText("D2 0600$ 1230$ 1400 #0").versions(V1_0).warnings(2).run(is(new Recurrence.Builder(Frequency.DAILY).interval(2).byHour(6).byHour(12).byHour(14).byMinute(0).byMinute(30).byMinute(0).build()));

		sensei.assertParseText("M2").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MINUTELY).interval(2).count(2).build()));
		sensei.assertParseText("M2 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MINUTELY).interval(2).build()));
		sensei.assertParseText("M2 20000101T010000Z").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MINUTELY).interval(2).until(new ICalDate(utc("2000-01-01 01:00:00"))).build()));
		sensei.assertParseText("M2 20000101T010000").versions(V1_0).run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.MINUTELY).interval(2).until(new ICalDate(date("2000-01-01 01:00:00"))).build();
				is(expected).check(property, context);

				assertEquals(1, context.getFloatingDates().size());
				assertEquals(0, context.getTimezonedDates().size());

				TimezonedDate timezonedDate = context.getFloatingDates().iterator().next();
				assertEquals(property, timezonedDate.getProperty());
				assertEquals(date("2000-01-01 01:00:00"), timezonedDate.getDate());

			}
		});

		sensei.assertParseText("D2 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.DAILY).interval(2).build()));
		sensei.assertParseText("D2 0600 1230 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.DAILY).interval(2).byHour(6).byHour(12).byMinute(0).byMinute(30).build()));
		sensei.assertParseText("D2 0600 1230 invalid #0").versions(V1_0).run(null, NumberFormatException.class);

		sensei.assertParseText("W2 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.WEEKLY).interval(2).build()));
		sensei.assertParseText("W2 MO WE #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.WEEKLY).interval(2).byDay(DayOfWeek.MONDAY).byDay(DayOfWeek.WEDNESDAY).build()));
		sensei.assertParseText("W2 MO WE AA #0").versions(V1_0).cannotParse();

		sensei.assertParseText("MP2 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MONTHLY).interval(2).build()));
		sensei.assertParseText("MP2 1+ 1- MO 1200 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MONTHLY).interval(2).byDay(1, DayOfWeek.MONDAY).byDay(-1, DayOfWeek.MONDAY).byHour(12).byMinute(0).build()));
		sensei.assertParseText("MP2 1- MO FR 1200 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MONTHLY).interval(2).byDay(-1, DayOfWeek.MONDAY).byDay(-1, DayOfWeek.FRIDAY).byHour(12).byMinute(0).build()));
		sensei.assertParseText("MP2 1- MO 1+ MO #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MONTHLY).interval(2).byDay(-1, DayOfWeek.MONDAY).byDay(1, DayOfWeek.MONDAY).build()));

		sensei.assertParseText("MD2 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MONTHLY).interval(2).build()));
		sensei.assertParseText("MD2 1+ 1- #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MONTHLY).interval(2).byMonthDay(1).byMonthDay(-1).build()));
		sensei.assertParseText("MD2 1+ LD #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.MONTHLY).interval(2).byMonthDay(1).byMonthDay(-1).build()));

		sensei.assertParseText("YM2 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.YEARLY).interval(2).build()));
		sensei.assertParseText("YM2 4 7 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.YEARLY).interval(2).byMonth(4).byMonth(7).build()));
		sensei.assertParseText("YM2 4 invalid #0").versions(V1_0).run(null, NumberFormatException.class);

		sensei.assertParseText("YD2 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.YEARLY).interval(2).build()));
		sensei.assertParseText("YD2 1 100 #0").versions(V1_0).run(is(new Recurrence.Builder(Frequency.YEARLY).interval(2).byYearDay(1).byYearDay(100).build()));
		sensei.assertParseText("YD2 4 invalid #0").versions(V1_0).run(null, NumberFormatException.class);
	}

	@Test
	public void parseText() {
		sensei.assertParseText("FREQ=WEEKLY;COUNT=5;INTERVAL=10;UNTIL=" + dateTimeStr + ";BYSECOND=58,59;BYMINUTE=3,4;BYHOUR=1,2;BYDAY=MO,TU,WE,TH,FR,SA,SU,5FR;BYMONTHDAY=1,2;BYYEARDAY=100,101;BYWEEKNO=1,2;BYMONTH=5,6;BYSETPOS=7,8,9;WKST=TU;X-NAME=one,two;X-RULE=three").versions(V2_0).run(fullCheck);
	}

	@Test
	public void parseText_until() {
		sensei.assertParseText("FREQ=WEEKLY;UNTIL=" + dateTimeStr).versions(V2_0).run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.WEEKLY).until(new ICalDate(new ICalDate(utc("2013-06-11 12:43:02")))).build();
				is(expected).check(property, context);

				assertEquals(0, context.getFloatingDates().size());
				assertEquals(0, context.getTimezonedDates().size());
			}
		});

		sensei.assertParseText("FREQ=WEEKLY;UNTIL=20130611T124302").param("TZID", "America/New_York").versions(V2_0).run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.WEEKLY).until(new ICalDate(date("2013-06-11 12:43:02"))).build();
				is(expected).check(property, context);

				assertEquals(0, context.getFloatingDates().size());
				assertEquals(1, context.getTimezonedDates().size());
				assertEquals(1, context.getTimezonedDates().get("America/New_York").size());

				TimezonedDate timezonedDate = context.getTimezonedDates().get("America/New_York").get(0);
				assertEquals(property, timezonedDate.getProperty());
				assertEquals(date("2013-06-11 12:43:02"), timezonedDate.getDate());
			}
		});

		sensei.assertParseText("FREQ=WEEKLY;UNTIL=20130611T124302").versions(V2_0).run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.WEEKLY).until(new ICalDate(date("2013-06-11 12:43:02"))).build();
				is(expected).check(property, context);

				assertEquals(1, context.getFloatingDates().size());
				assertEquals(0, context.getTimezonedDates().size());

				TimezonedDate timezonedDate = context.getFloatingDates().iterator().next();
				assertEquals(property, timezonedDate.getProperty());
				assertEquals(date("2013-06-11 12:43:02"), timezonedDate.getDate());
			}
		});
	}

	@Test
	public void parseText_invalid_values() {
		sensei.assertParseText("FREQ=W;COUNT=a;INTERVAL=b;UNTIL=invalid;BYSECOND=58,c,59;BYMINUTE=3,d,4;BYHOUR=1,e,2;BYDAY=f,MO,TU,WE,TH,FR,SA,SU,5FR,fFR;BYMONTHDAY=1,g,2;BYYEARDAY=100,h,101;BYWEEKNO=1,w,2;BYMONTH=5,i,6;BYSETPOS=7,8,j,9;WKST=k").versions(V2_0).warnings(15).run(invalidValuesCheck);
	}

	@Test
	public void parseText_invalid_component() {
		sensei.assertParseText("FREQ=WEEKLY;no equals;COUNT=5").versions(V2_0).run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence recur = property.getValue();
				assertEquals(Frequency.WEEKLY, recur.getFrequency());
				assertIntEquals(5, recur.getCount());
				assertNull(recur.getInterval());
				assertNull(recur.getUntil());
				assertEquals(Arrays.asList(), recur.getByMinute());
				assertEquals(Arrays.asList(), recur.getByHour());
				assertEquals(Arrays.asList(), recur.getByDay());
				assertEquals(Arrays.asList(), recur.getByMonthDay());
				assertEquals(Arrays.asList(), recur.getByYearDay());
				assertEquals(Arrays.asList(), recur.getByMonth());
				assertEquals(Arrays.asList(), recur.getBySetPos());
				assertEquals(Arrays.asList(), recur.getByWeekNo());
				assertNull(recur.getWorkweekStarts());

				ListMultimap<String, String> expected = new ListMultimap<String, String>();
				expected.put("NO EQUALS", "");
				assertEquals(expected.asMap(), recur.getXRules());
			}
		});
	}

	@Test
	public void parseText_empty() {
		sensei.assertParseText("").run(emptyCheck);
	}

	@Test
	public void writeXml_multiples() {
		//@formatter:off
		sensei.assertWriteXml(withMultiple).run(
		"<recur>" +
			"<freq>WEEKLY</freq>" +
			"<count>5</count>" +
			"<interval>10</interval>" +
			"<bysecond>58</bysecond>" +
			"<bysecond>59</bysecond>" +
			"<byminute>3</byminute>" +
			"<byminute>4</byminute>" +
			"<byhour>1</byhour>" +
			"<byhour>2</byhour>" +
			"<byday>MO</byday>" +
			"<byday>TU</byday>" +
			"<byday>WE</byday>" +
			"<byday>TH</byday>" +
			"<byday>FR</byday>" +
			"<byday>SA</byday>" +
			"<byday>SU</byday>" +
			"<byday>5FR</byday>" +
			"<bymonthday>1</bymonthday>" +
			"<bymonthday>2</bymonthday>" +
			"<byyearday>100</byyearday>" +
			"<byyearday>101</byyearday>" +
			"<byweekno>1</byweekno>" +
			"<byweekno>2</byweekno>" +
			"<bymonth>5</bymonth>" +
			"<bymonth>6</bymonth>" +
			"<bysetpos>7</bysetpos>" +
			"<bysetpos>8</bysetpos>" +
			"<bysetpos>9</bysetpos>" +
			"<wkst>TU</wkst>" +
			"<x-name>one</x-name>" +
			"<x-name>two</x-name>" +
			"<x-rule>three</x-rule>" +
		"</recur>");
		//@formatter:on
	}

	@Test
	public void writeXml_singles() {
		//@formatter:off
		sensei.assertWriteXml(withSingle).run(
		"<recur>" +
			"<freq>WEEKLY</freq>" +
			"<count>5</count>" +
			"<interval>10</interval>" +
			"<bysecond>58</bysecond>" +
			"<byminute>3</byminute>" +
			"<byhour>1</byhour>" +
			"<byday>FR</byday>" +
			"<bymonthday>1</bymonthday>" +
			"<byyearday>100</byyearday>" +
			"<byweekno>1</byweekno>" +
			"<bymonth>5</bymonth>" +
			"<bysetpos>7</bysetpos>" +
			"<wkst>TU</wkst>" +
			"<x-name>one</x-name>" +
			"<x-rule>three</x-rule>" +
		"</recur>");
		//@formatter:on
	}

	@Test
	public void writeXml_until_datetime() {
		//@formatter:off
		sensei.assertWriteXml(withUntilDateTime).run(
		"<recur>" +
			"<freq>WEEKLY</freq>" +
			"<until>" + dateTimeStrExt + "</until>" +
		"</recur>");

		sensei.assertWriteXml(withUntilDate).run(
		"<recur>" +
			"<freq>WEEKLY</freq>" +
			"<until>" + dateStrExt + "</until>" +
		"</recur>");
		//@formatter:on
	}

	@Test
	public void writeXml_empty() {
		sensei.assertWriteXml(empty).run("<recur/>");
	}

	@Test
	public void parseXml() {
		//@formatter:off
		sensei.assertParseXml(
		"<recur>" +
			"<freq>WEEKLY</freq>" +
			"<count>5</count>" +
			"<interval>10</interval>" +
			"<until>" + dateTimeStrExt + "</until>" +
			"<bysecond>58</bysecond>" +
			"<bysecond>59</bysecond>" +
			"<byminute>3</byminute>" +
			"<byminute>4</byminute>" +
			"<byhour>1</byhour>" +
			"<byhour>2</byhour>" +
			"<byday>MO</byday>" +
			"<byday>TU</byday>" +
			"<byday>WE</byday>" +
			"<byday>TH</byday>" +
			"<byday>FR</byday>" +
			"<byday>SA</byday>" +
			"<byday>SU</byday>" +
			"<byday>5FR</byday>" +
			"<bymonthday>1</bymonthday>" +
			"<bymonthday>2</bymonthday>" +
			"<byyearday>100</byyearday>" +
			"<byyearday>101</byyearday>" +
			"<byweekno>1</byweekno>" +
			"<byweekno>2</byweekno>" +
			"<bymonth>5</bymonth>" +
			"<bymonth>6</bymonth>" +
			"<bysetpos>7</bysetpos>" +
			"<bysetpos>8</bysetpos>" +
			"<bysetpos>9</bysetpos>" +
			"<wkst>TU</wkst>" +
			"<x-name>one</x-name>" +
			"<x-name>two</x-name>" +
			"<x-rule>three</x-rule>" +
		"</recur>"
		).run(fullCheck);
		//@formatter:on
	}

	@Test
	public void parseXml_until() {
		sensei.assertParseXml("<recur><freq>WEEKLY</freq><until>" + dateTimeStrExt + "</until></recur>").run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.WEEKLY).until(new ICalDate(utc("2013-06-11 12:43:02"))).build();
				is(expected).check(property, context);

				assertEquals(0, context.getFloatingDates().size());
				assertEquals(0, context.getTimezonedDates().size());
			}
		});

		sensei.assertParseXml("<recur><freq>WEEKLY</freq><until>2013-06-11T12:43:02</until></recur>").param("TZID", "America/New_York").run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.WEEKLY).until(new ICalDate(date("2013-06-11 12:43:02"))).build();
				is(expected).check(property, context);

				assertEquals(0, context.getFloatingDates().size());
				assertEquals(1, context.getTimezonedDates().size());
				assertEquals(1, context.getTimezonedDates().get("America/New_York").size());

				TimezonedDate timezonedDate = context.getTimezonedDates().get("America/New_York").get(0);
				assertEquals(property, timezonedDate.getProperty());
				assertEquals(date("2013-06-11 12:43:02"), timezonedDate.getDate());
			}
		});

		sensei.assertParseXml("<recur><freq>WEEKLY</freq><until>2013-06-11T12:43:02</until></recur>").run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.WEEKLY).until(new ICalDate(date("2013-06-11 12:43:02"))).build();
				is(expected).check(property, context);

				assertEquals(1, context.getFloatingDates().size());
				assertEquals(0, context.getTimezonedDates().size());

				TimezonedDate timezonedDate = context.getFloatingDates().iterator().next();
				assertEquals(property, timezonedDate.getProperty());
				assertEquals(date("2013-06-11 12:43:02"), timezonedDate.getDate());
			}
		});
	}

	@Test
	public void parseXml_invalid_values() {
		//@formatter:off
		sensei.assertParseXml(
		"<recur>" +
			"<freq>W</freq>" +
			"<count>a</count>" +
			"<interval>b</interval>" +
			"<until>invalid</until>" +
			"<bysecond>58</bysecond>" +
			"<bysecond>c</bysecond>" +
			"<bysecond>59</bysecond>" +
			"<byminute>3</byminute>" +
			"<byminute>d</byminute>" +
			"<byminute>4</byminute>" +
			"<byhour>1</byhour>" +
			"<byhour>e</byhour>" +
			"<byhour>2</byhour>" +
			"<byday>f</byday>" +
			"<byday>MO</byday>" +
			"<byday>TU</byday>" +
			"<byday>WE</byday>" +
			"<byday>TH</byday>" +
			"<byday>FR</byday>" +
			"<byday>SA</byday>" +
			"<byday>SU</byday>" +
			"<byday>5FR</byday>" +
			"<byday>fFR</byday>" +
			"<bymonthday>1</bymonthday>" +
			"<bymonthday>g</bymonthday>" +
			"<bymonthday>2</bymonthday>" +
			"<byyearday>100</byyearday>" +
			"<byyearday>h</byyearday>" +
			"<byyearday>101</byyearday>" +
			"<byweekno>1</byweekno>" +
			"<byweekno>w</byweekno>" +
			"<byweekno>2</byweekno>" +
			"<bymonth>5</bymonth>" +
			"<bymonth>i</bymonth>" +
			"<bymonth>6</bymonth>" +
			"<bysetpos>7</bysetpos>" +
			"<bysetpos>8</bysetpos>" +
			"<bysetpos>j</bysetpos>" +
			"<bysetpos>9</bysetpos>" +
			"<wkst>k</wkst>" +
		"</recur>"
		).warnings(15).run(invalidValuesCheck);
		//@formatter:on
	}

	@Test
	public void parseXml_empty() {
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson_multiples() {
		JCalValue actual = sensei.assertWriteJson(withMultiple).run();

		Map<String, JsonValue> expected = new LinkedHashMap<String, JsonValue>();
		expected.put("freq", new JsonValue("WEEKLY"));
		expected.put("count", new JsonValue(5));
		expected.put("interval", new JsonValue(10));
		expected.put("bysecond", new JsonValue(Arrays.asList(new JsonValue(58), new JsonValue(59))));
		expected.put("byminute", new JsonValue(Arrays.asList(new JsonValue(3), new JsonValue(4))));
		expected.put("byhour", new JsonValue(Arrays.asList(new JsonValue(1), new JsonValue(2))));
		expected.put("byday", new JsonValue(Arrays.asList(new JsonValue("MO"), new JsonValue("TU"), new JsonValue("WE"), new JsonValue("TH"), new JsonValue("FR"), new JsonValue("SA"), new JsonValue("SU"), new JsonValue("5FR"))));
		expected.put("bymonthday", new JsonValue(Arrays.asList(new JsonValue(1), new JsonValue(2))));
		expected.put("byyearday", new JsonValue(Arrays.asList(new JsonValue(100), new JsonValue(101))));
		expected.put("byweekno", new JsonValue(Arrays.asList(new JsonValue(1), new JsonValue(2))));
		expected.put("bymonth", new JsonValue(Arrays.asList(new JsonValue(5), new JsonValue(6))));
		expected.put("bysetpos", new JsonValue(Arrays.asList(new JsonValue(7), new JsonValue(8), new JsonValue(9))));
		expected.put("wkst", new JsonValue("TU"));
		expected.put("x-name", new JsonValue(Arrays.asList(new JsonValue("one"), new JsonValue("two"))));
		expected.put("x-rule", new JsonValue("three"));

		assertEquals(expected, actual.getValues().get(0).getObject());
	}

	@Test
	public void writeJson_singles() {
		JCalValue actual = sensei.assertWriteJson(withSingle).run();

		Map<String, JsonValue> expected = new LinkedHashMap<String, JsonValue>();
		expected.put("freq", new JsonValue("WEEKLY"));
		expected.put("count", new JsonValue(5));
		expected.put("interval", new JsonValue(10));
		expected.put("bysecond", new JsonValue(58));
		expected.put("byminute", new JsonValue(3));
		expected.put("byhour", new JsonValue(1));
		expected.put("byday", new JsonValue("FR"));
		expected.put("bymonthday", new JsonValue(1));
		expected.put("byyearday", new JsonValue(100));
		expected.put("byweekno", new JsonValue(1));
		expected.put("bymonth", new JsonValue(5));
		expected.put("bysetpos", new JsonValue(7));
		expected.put("wkst", new JsonValue("TU"));
		expected.put("x-name", new JsonValue("one"));
		expected.put("x-rule", new JsonValue("three"));

		assertEquals(expected, actual.getValues().get(0).getObject());
	}

	@Test
	public void writeJson_until_datetime() {
		JCalValue actual = sensei.assertWriteJson(withUntilDateTime).run();

		Map<String, JsonValue> expected = new LinkedHashMap<String, JsonValue>();
		expected.put("freq", new JsonValue("WEEKLY"));
		expected.put("until", new JsonValue(dateTimeStrExt));

		assertEquals(expected, actual.getValues().get(0).getObject());
	}

	@Test
	public void writeJson_until_date() {
		JCalValue actual = sensei.assertWriteJson(withUntilDate).run();

		Map<String, JsonValue> expected = new LinkedHashMap<String, JsonValue>();
		expected.put("freq", new JsonValue("WEEKLY"));
		expected.put("until", new JsonValue(dateStrExt));

		assertEquals(expected, actual.getValues().get(0).getObject());
	}

	@Test
	public void writeJson_empty() {
		sensei.assertWriteJson(empty).run((String) null);
	}

	@Test
	public void parseJson() {
		ListMultimap<String, Object> map = new ListMultimap<String, Object>();
		map.put("freq", "WEEKLY");
		map.put("count", 5);
		map.put("interval", 10);
		map.put("until", dateTimeStrExt);
		map.put("bysecond", 58);
		map.put("bysecond", 59);
		map.put("byminute", 3);
		map.put("byminute", 4);
		map.put("byhour", 1);
		map.put("byhour", 2);
		map.put("byday", "MO");
		map.put("byday", "TU");
		map.put("byday", "WE");
		map.put("byday", "TH");
		map.put("byday", "FR");
		map.put("byday", "SA");
		map.put("byday", "SU");
		map.put("byday", "5FR");
		map.put("bymonthday", 1);
		map.put("bymonthday", 2);
		map.put("byyearday", 100);
		map.put("byyearday", 101);
		map.put("byweekno", 1);
		map.put("byweekno", 2);
		map.put("bymonth", 5);
		map.put("bymonth", 6);
		map.put("bysetpos", 7);
		map.put("bysetpos", 8);
		map.put("bysetpos", 9);
		map.put("wkst", "TU");
		map.put("x-name", "one");
		map.put("x-name", "two");
		map.put("x-rule", "three");

		sensei.assertParseJson(JCalValue.object(map)).run(fullCheck);
	}

	@Test
	public void parseJson_until() {
		ListMultimap<String, Object> map = new ListMultimap<String, Object>();
		map.put("freq", "WEEKLY");
		map.put("until", dateTimeStrExt);
		sensei.assertParseJson(JCalValue.object(map)).run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.WEEKLY).until(new ICalDate(utc("2013-06-11 12:43:02"))).build();
				is(expected).check(property, context);

				assertEquals(0, context.getFloatingDates().size());
				assertEquals(0, context.getTimezonedDates().size());
			}
		});

		map = new ListMultimap<String, Object>();
		map.put("freq", "WEEKLY");
		map.put("until", "2013-06-11T12:43:02");
		sensei.assertParseJson(JCalValue.object(map)).param("TZID", "America/New_York").run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.WEEKLY).until(new ICalDate(date("2013-06-11 12:43:02"))).build();
				is(expected).check(property, context);

				assertEquals(0, context.getFloatingDates().size());
				assertEquals(1, context.getTimezonedDates().size());
				assertEquals(1, context.getTimezonedDates().get("America/New_York").size());

				TimezonedDate timezonedDate = context.getTimezonedDates().get("America/New_York").get(0);
				assertEquals(property, timezonedDate.getProperty());
				assertEquals(date("2013-06-11 12:43:02"), timezonedDate.getDate());
			}
		});

		map = new ListMultimap<String, Object>();
		map.put("freq", "WEEKLY");
		map.put("until", "2013-06-11T12:43:02");
		sensei.assertParseJson(JCalValue.object(map)).run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence expected = new Recurrence.Builder(Frequency.WEEKLY).until(new ICalDate(date("2013-06-11 12:43:02"))).build();
				is(expected).check(property, context);

				assertEquals(1, context.getFloatingDates().size());
				assertEquals(0, context.getTimezonedDates().size());

				TimezonedDate timezonedDate = context.getFloatingDates().iterator().next();
				assertEquals(property, timezonedDate.getProperty());

				assertEquals(date("2013-06-11 12:43:02"), timezonedDate.getDate());
			}
		});
	}

	@Test
	public void parseJson_invalid_values() {
		ListMultimap<String, Object> map = new ListMultimap<String, Object>();
		map.put("freq", "W");
		map.put("count", "a");
		map.put("interval", "b");
		map.put("until", "invalid");
		map.put("bysecond", 58);
		map.put("bysecond", "c");
		map.put("bysecond", 59);
		map.put("byminute", 3);
		map.put("byminute", "d");
		map.put("byminute", 4);
		map.put("byhour", 1);
		map.put("byhour", "e");
		map.put("byhour", 2);
		map.put("byday", "f");
		map.put("byday", "MO");
		map.put("byday", "TU");
		map.put("byday", "WE");
		map.put("byday", "TH");
		map.put("byday", "FR");
		map.put("byday", "SA");
		map.put("byday", "SU");
		map.put("byday", "5FR");
		map.put("byday", "fFR");
		map.put("bymonthday", 1);
		map.put("bymonthday", "g");
		map.put("bymonthday", 2);
		map.put("byyearday", 100);
		map.put("byyearday", "h");
		map.put("byyearday", 101);
		map.put("byweekno", 1);
		map.put("byweekno", "w");
		map.put("byweekno", 2);
		map.put("bymonth", 5);
		map.put("bymonth", "i");
		map.put("bymonth", 6);
		map.put("bysetpos", 7);
		map.put("bysetpos", 8);
		map.put("bysetpos", "j");
		map.put("bysetpos", 9);
		map.put("wkst", "k");

		sensei.assertParseJson(JCalValue.object(map)).warnings(15).run(invalidValuesCheck);
	}

	@Test
	public void parseJson_empty() {
		sensei.assertParseJson("").run(emptyCheck);
	}

	public static class RecurrencePropertyMarshallerImpl extends RecurrencePropertyScribe<RecurrenceProperty> {
		public RecurrencePropertyMarshallerImpl() {
			super(RecurrenceProperty.class, "RECURRENCE");
		}

		@Override
		protected RecurrenceProperty newInstance(Recurrence recur) {
			return new RecurrenceProperty(recur);
		}
	}

	private final Check<RecurrenceProperty> fullCheck = new Check<RecurrenceProperty>() {
		public void check(RecurrenceProperty property, ParseContext context) {
			Recurrence recur = property.getValue();
			assertEquals(Frequency.WEEKLY, recur.getFrequency());
			assertIntEquals(5, recur.getCount());
			assertIntEquals(10, recur.getInterval());
			assertEquals(datetime, recur.getUntil());
			assertEquals(Arrays.asList(3, 4), recur.getByMinute());
			assertEquals(Arrays.asList(1, 2), recur.getByHour());

			//@formatter:off
			assertEquals(Arrays.asList(
				new ByDay(DayOfWeek.MONDAY),
				new ByDay(DayOfWeek.TUESDAY),
				new ByDay(DayOfWeek.WEDNESDAY),
				new ByDay(DayOfWeek.THURSDAY),
				new ByDay(DayOfWeek.FRIDAY),
				new ByDay(DayOfWeek.SATURDAY),
				new ByDay(DayOfWeek.SUNDAY),
				new ByDay(5, DayOfWeek.FRIDAY)
			), recur.getByDay());
			//@formatter:on

			assertEquals(Arrays.asList(1, 2), recur.getByMonthDay());
			assertEquals(Arrays.asList(100, 101), recur.getByYearDay());
			assertEquals(Arrays.asList(5, 6), recur.getByMonth());
			assertEquals(Arrays.asList(7, 8, 9), recur.getBySetPos());
			assertEquals(Arrays.asList(1, 2), recur.getByWeekNo());
			assertEquals(DayOfWeek.TUESDAY, recur.getWorkweekStarts());

			Map<String, List<String>> expected = new HashMap<String, List<String>>();
			expected.put("X-NAME", Arrays.asList("one", "two"));
			expected.put("X-RULE", Arrays.asList("three"));
			assertEquals(expected, recur.getXRules());
		}
	};

	private final Check<RecurrenceProperty> invalidValuesCheck = new Check<RecurrenceProperty>() {
		public void check(RecurrenceProperty property, ParseContext context) {
			Recurrence recur = property.getValue();
			assertNull(recur.getFrequency());
			assertNull(recur.getCount());
			assertNull(recur.getInterval());
			assertNull(recur.getUntil());
			assertEquals(Arrays.asList(3, 4), recur.getByMinute());
			assertEquals(Arrays.asList(1, 2), recur.getByHour());

			//@formatter:off
			assertEquals(Arrays.asList(
				new ByDay(DayOfWeek.MONDAY),
				new ByDay(DayOfWeek.TUESDAY),
				new ByDay(DayOfWeek.WEDNESDAY),
				new ByDay(DayOfWeek.THURSDAY),
				new ByDay(DayOfWeek.FRIDAY),
				new ByDay(DayOfWeek.SATURDAY),
				new ByDay(DayOfWeek.SUNDAY),
				new ByDay(5, DayOfWeek.FRIDAY)
			), recur.getByDay());
			//@formatter:on

			assertEquals(Arrays.asList(1, 2), recur.getByMonthDay());
			assertEquals(Arrays.asList(100, 101), recur.getByYearDay());
			assertEquals(Arrays.asList(5, 6), recur.getByMonth());
			assertEquals(Arrays.asList(7, 8, 9), recur.getBySetPos());
			assertEquals(Arrays.asList(1, 2), recur.getByWeekNo());
			assertNull(recur.getWorkweekStarts());
			assertTrue(recur.getXRules().isEmpty());
		}
	};

	private final Check<RecurrenceProperty> emptyCheck = new Check<RecurrenceProperty>() {
		public void check(RecurrenceProperty property, ParseContext context) {
			Recurrence recur = property.getValue();
			assertNull(recur.getFrequency());
			assertNull(recur.getCount());
			assertNull(recur.getInterval());
			assertNull(recur.getUntil());
			assertEquals(Arrays.asList(), recur.getByMinute());
			assertEquals(Arrays.asList(), recur.getByHour());
			assertEquals(Arrays.asList(), recur.getByDay());
			assertEquals(Arrays.asList(), recur.getByMonthDay());
			assertEquals(Arrays.asList(), recur.getByYearDay());
			assertEquals(Arrays.asList(), recur.getByMonth());
			assertEquals(Arrays.asList(), recur.getBySetPos());
			assertEquals(Arrays.asList(), recur.getByWeekNo());
			assertNull(recur.getWorkweekStarts());
			assertTrue(recur.getXRules().isEmpty());
		}
	};

	private Check<RecurrenceProperty> is(final Recurrence expected) {
		return new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property, ParseContext context) {
				Recurrence actual = property.getValue();
				assertEquals(expected, actual);
			}
		};
	}

	private static TimezoneInfo floating() {
		TimezoneInfo tzinfo = new TimezoneInfo();
		tzinfo.setGlobalFloatingTime(true);
		return tzinfo;
	}

	private static TimezoneInfo americaNewYork() {
		String id = "America/New_York";
		TimeZone timezone = TimeZone.getTimeZone(id);
		VTimezone component = new VTimezone(id);

		TimezoneInfo tzinfo = new TimezoneInfo();
		tzinfo.assign(component, timezone);
		tzinfo.setDefaultTimeZone(timezone);
		return tzinfo;
	}
}
