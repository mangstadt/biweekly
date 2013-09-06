package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.io.json.JsonValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.RecurrenceProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
import biweekly.util.ListMultimap;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;

/*
 Copyright (c) 2013, Michael Angstadt
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
public class RecurrencePropertyMarshallerTest {
	private final RecurrencePropertyMarshaller<RecurrenceProperty> marshaller = new RecurrencePropertyMarshaller<RecurrenceProperty>(RecurrenceProperty.class, "TEST") {
		@Override
		protected RecurrenceProperty newInstance(Recurrence recur) {
			return new RecurrenceProperty(recur);
		}
	};

	private final RecurrenceProperty multipleValues;
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

		multipleValues = new RecurrenceProperty(builder.build());
	}

	private final RecurrenceProperty singleValues;
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

		singleValues = new RecurrenceProperty(builder.build());
	}

	private final Date datetime;
	{
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.clear();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DATE, 11);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 43);
		c.set(Calendar.SECOND, 2);
		datetime = c.getTime();
	}

	private final RecurrenceProperty untilDate = new RecurrenceProperty(new Recurrence.Builder(Frequency.WEEKLY).until(datetime, false).build());
	private final RecurrenceProperty untilDateTime = new RecurrenceProperty(new Recurrence.Builder(Frequency.WEEKLY).until(datetime).build());

	@Test
	public void writeText_multiples() {
		String actual = marshaller.writeText(multipleValues);
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
		String actual = marshaller.writeText(singleValues);
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
		String actual = marshaller.writeText(untilDateTime);

		assertEquals("FREQ=WEEKLY;UNTIL=20130611T134302Z", actual);
	}

	@Test
	public void writeText_until_date() {
		String actual = marshaller.writeText(untilDate);

		assertEquals("FREQ=WEEKLY;UNTIL=20130611", actual);
	}

	@Test
	public void parseText() {
		String value = "FREQ=WEEKLY;COUNT=5;INTERVAL=10;UNTIL=20130611T134302Z;BYSECOND=58,59;BYMINUTE=3,4;BYHOUR=1,2;BYDAY=MO,TU,WE,TH,FR,SA,SU,5FR;BYMONTHDAY=1,2;BYYEARDAY=100,101;BYWEEKNO=1,2;BYMONTH=5,6;BYSETPOS=7,8,9;WKST=TU;X-NAME=one,two;X-RULE=three";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceProperty> result = marshaller.parseText(value, ICalDataType.RECUR, params);

		Recurrence prop = result.getProperty().getValue();
		assertEquals(Frequency.WEEKLY, prop.getFrequency());
		assertIntEquals(5, prop.getCount());
		assertIntEquals(10, prop.getInterval());
		assertEquals(datetime, prop.getUntil());
		assertEquals(Arrays.asList(3, 4), prop.getByMinute());
		assertEquals(Arrays.asList(1, 2), prop.getByHour());
		assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY), prop.getByDay());
		assertEquals(Arrays.asList(null, null, null, null, null, null, null, Integer.valueOf(5)), prop.getByDayPrefixes());
		assertEquals(Arrays.asList(1, 2), prop.getByMonthDay());
		assertEquals(Arrays.asList(100, 101), prop.getByYearDay());
		assertEquals(Arrays.asList(5, 6), prop.getByMonth());
		assertEquals(Arrays.asList(7, 8, 9), prop.getBySetPos());
		assertEquals(Arrays.asList(1, 2), prop.getByWeekNo());
		assertEquals(DayOfWeek.TUESDAY, prop.getWorkweekStarts());

		Map<String, List<String>> expected = new HashMap<String, List<String>>();
		expected.put("X-NAME", Arrays.asList("one", "two"));
		expected.put("X-RULE", Arrays.asList("three"));
		assertEquals(expected, prop.getXRules());

		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_invalid_values() {
		String value = "FREQ=W;COUNT=a;INTERVAL=b;UNTIL=invalid;BYSECOND=58,c,59;BYMINUTE=3,d,4;BYHOUR=1,e,2;BYDAY=f,MO,TU,WE,TH,FR,SA,SU,5FR,fFR;BYMONTHDAY=1,g,2;BYYEARDAY=100,h,101;BYWEEKNO=1,w,2;BYMONTH=5,i,6;BYSETPOS=7,8,j,9;WKST=k";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceProperty> result = marshaller.parseText(value, ICalDataType.RECUR, params);

		Recurrence prop = result.getProperty().getValue();
		assertNull(prop.getFrequency());
		assertNull(prop.getCount());
		assertNull(prop.getInterval());
		assertNull(prop.getUntil());
		assertEquals(Arrays.asList(3, 4), prop.getByMinute());
		assertEquals(Arrays.asList(1, 2), prop.getByHour());
		assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY), prop.getByDay());
		assertEquals(Arrays.asList(null, null, null, null, null, null, null, Integer.valueOf(5)), prop.getByDayPrefixes());
		assertEquals(Arrays.asList(1, 2), prop.getByMonthDay());
		assertEquals(Arrays.asList(100, 101), prop.getByYearDay());
		assertEquals(Arrays.asList(5, 6), prop.getByMonth());
		assertEquals(Arrays.asList(7, 8, 9), prop.getBySetPos());
		assertEquals(Arrays.asList(1, 2), prop.getByWeekNo());
		assertNull(prop.getWorkweekStarts());
		assertWarnings(15, result.getWarnings());
	}

	@Test
	public void parseText_invalid_component() {
		String value = "FREQ=WEEKLY;no equals;COUNT=5";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceProperty> result = marshaller.parseText(value, ICalDataType.RECUR, params);

		Recurrence prop = result.getProperty().getValue();
		assertEquals(Frequency.WEEKLY, prop.getFrequency());
		assertIntEquals(5, prop.getCount());
		assertNull(prop.getInterval());
		assertNull(prop.getUntil());
		assertEquals(Arrays.asList(), prop.getByMinute());
		assertEquals(Arrays.asList(), prop.getByHour());
		assertEquals(Arrays.asList(), prop.getByDay());
		assertEquals(Arrays.asList(), prop.getByDayPrefixes());
		assertEquals(Arrays.asList(), prop.getByMonthDay());
		assertEquals(Arrays.asList(), prop.getByYearDay());
		assertEquals(Arrays.asList(), prop.getByMonth());
		assertEquals(Arrays.asList(), prop.getBySetPos());
		assertEquals(Arrays.asList(), prop.getByWeekNo());
		assertNull(prop.getWorkweekStarts());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void writeXml_multiples() {
		//@formatter:off
		assertWriteXml(
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
		"</recur>", multipleValues, marshaller);
		//@formatter:on
	}

	@Test
	public void writeXml_singles() {
		//@formatter:off
		assertWriteXml(
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
		"</recur>", singleValues, marshaller);
		//@formatter:on
	}

	@Test
	public void writeXml_until_datetime() {
		//@formatter:off
		assertWriteXml(
		"<recur>" +
			"<freq>WEEKLY</freq>" +
			"<until>2013-06-11T13:43:02Z</until>" +
		"</recur>", untilDateTime, marshaller);
		//@formatter:on
	}

	@Test
	public void writeXml_until_date() {
		//@formatter:off
		assertWriteXml(
		"<recur>" +
			"<freq>WEEKLY</freq>" +
			"<until>2013-06-11</until>" +
		"</recur>", untilDate, marshaller);
		//@formatter:on
	}

	@Test
	public void parseXml() {
		//@formatter:off
		Result<RecurrenceProperty> result = parseXCalProperty(
		"<recur>" +
			"<freq>WEEKLY</freq>" +
			"<count>5</count>" +
			"<interval>10</interval>" +
			"<until>2013-06-11T13:43:02Z</until>" +
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
		"</recur>", marshaller);
		//@formatter:on

		Recurrence prop = result.getProperty().getValue();
		assertEquals(Frequency.WEEKLY, prop.getFrequency());
		assertIntEquals(5, prop.getCount());
		assertIntEquals(10, prop.getInterval());
		assertEquals(datetime, prop.getUntil());
		assertEquals(Arrays.asList(3, 4), prop.getByMinute());
		assertEquals(Arrays.asList(1, 2), prop.getByHour());
		assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY), prop.getByDay());
		assertEquals(Arrays.asList(null, null, null, null, null, null, null, Integer.valueOf(5)), prop.getByDayPrefixes());
		assertEquals(Arrays.asList(1, 2), prop.getByMonthDay());
		assertEquals(Arrays.asList(100, 101), prop.getByYearDay());
		assertEquals(Arrays.asList(5, 6), prop.getByMonth());
		assertEquals(Arrays.asList(7, 8, 9), prop.getBySetPos());
		assertEquals(Arrays.asList(1, 2), prop.getByWeekNo());
		assertEquals(DayOfWeek.TUESDAY, prop.getWorkweekStarts());

		Map<String, List<String>> expected = new HashMap<String, List<String>>();
		expected.put("X-NAME", Arrays.asList("one", "two"));
		expected.put("X-RULE", Arrays.asList("three"));
		assertEquals(expected, prop.getXRules());

		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_invalid_values() {
		//@formatter:off
		Result<RecurrenceProperty> result = parseXCalProperty(
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
		"</recur>", marshaller);
		//@formatter:on

		Recurrence prop = result.getProperty().getValue();
		assertNull(prop.getFrequency());
		assertNull(prop.getCount());
		assertNull(prop.getInterval());
		assertNull(prop.getUntil());
		assertEquals(Arrays.asList(3, 4), prop.getByMinute());
		assertEquals(Arrays.asList(1, 2), prop.getByHour());
		assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY), prop.getByDay());
		assertEquals(Arrays.asList(null, null, null, null, null, null, null, Integer.valueOf(5)), prop.getByDayPrefixes());
		assertEquals(Arrays.asList(1, 2), prop.getByMonthDay());
		assertEquals(Arrays.asList(100, 101), prop.getByYearDay());
		assertEquals(Arrays.asList(5, 6), prop.getByMonth());
		assertEquals(Arrays.asList(7, 8, 9), prop.getBySetPos());
		assertEquals(Arrays.asList(1, 2), prop.getByWeekNo());
		assertNull(prop.getWorkweekStarts());
		assertWarnings(15, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_empty() {
		parseXCalProperty("", marshaller);
	}

	@Test
	public void writeJson_multiples() {
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

		JCalValue actual = marshaller.writeJson(multipleValues);
		assertEquals(expected, actual.getValues().get(0).getObject());
	}

	@Test
	public void writeJson_singles() {
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

		JCalValue actual = marshaller.writeJson(singleValues);
		assertEquals(expected, actual.getValues().get(0).getObject());
	}

	@Test
	public void writeJson_until_datetime() {
		Map<String, JsonValue> expected = new LinkedHashMap<String, JsonValue>();
		expected.put("freq", new JsonValue("WEEKLY"));
		expected.put("until", new JsonValue("2013-06-11T13:43:02Z"));

		JCalValue actual = marshaller.writeJson(untilDateTime);
		assertEquals(expected, actual.getValues().get(0).getObject());
	}

	@Test
	public void writeJson_until_date() {
		Map<String, JsonValue> expected = new LinkedHashMap<String, JsonValue>();
		expected.put("freq", new JsonValue("WEEKLY"));
		expected.put("until", new JsonValue("2013-06-11"));

		JCalValue actual = marshaller.writeJson(untilDate);
		assertEquals(expected, actual.getValues().get(0).getObject());
	}

	@Test
	public void parseJson() {
		ListMultimap<String, Object> map = new ListMultimap<String, Object>();
		map.put("freq", "WEEKLY");
		map.put("count", 5);
		map.put("interval", 10);
		map.put("until", "2013-06-11T13:43:02Z");
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
		Result<RecurrenceProperty> result = marshaller.parseJson(JCalValue.object(map), ICalDataType.RECUR, new ICalParameters());

		Recurrence prop = result.getProperty().getValue();
		assertEquals(Frequency.WEEKLY, prop.getFrequency());
		assertIntEquals(5, prop.getCount());
		assertIntEquals(10, prop.getInterval());
		assertEquals(datetime, prop.getUntil());
		assertEquals(Arrays.asList(3, 4), prop.getByMinute());
		assertEquals(Arrays.asList(1, 2), prop.getByHour());
		assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY), prop.getByDay());
		assertEquals(Arrays.asList(null, null, null, null, null, null, null, Integer.valueOf(5)), prop.getByDayPrefixes());
		assertEquals(Arrays.asList(1, 2), prop.getByMonthDay());
		assertEquals(Arrays.asList(100, 101), prop.getByYearDay());
		assertEquals(Arrays.asList(5, 6), prop.getByMonth());
		assertEquals(Arrays.asList(7, 8, 9), prop.getBySetPos());
		assertEquals(Arrays.asList(1, 2), prop.getByWeekNo());
		assertEquals(DayOfWeek.TUESDAY, prop.getWorkweekStarts());

		Map<String, List<String>> expected = new HashMap<String, List<String>>();
		expected.put("X-NAME", Arrays.asList("one", "two"));
		expected.put("X-RULE", Arrays.asList("three"));
		assertEquals(expected, prop.getXRules());

		assertWarnings(0, result.getWarnings());
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
		Result<RecurrenceProperty> result = marshaller.parseJson(JCalValue.object(map), ICalDataType.RECUR, new ICalParameters());

		Recurrence prop = result.getProperty().getValue();
		assertNull(prop.getFrequency());
		assertNull(prop.getCount());
		assertNull(prop.getInterval());
		assertNull(prop.getUntil());
		assertEquals(Arrays.asList(3, 4), prop.getByMinute());
		assertEquals(Arrays.asList(1, 2), prop.getByHour());
		assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY), prop.getByDay());
		assertEquals(Arrays.asList(null, null, null, null, null, null, null, Integer.valueOf(5)), prop.getByDayPrefixes());
		assertEquals(Arrays.asList(1, 2), prop.getByMonthDay());
		assertEquals(Arrays.asList(100, 101), prop.getByYearDay());
		assertEquals(Arrays.asList(5, 6), prop.getByMonth());
		assertEquals(Arrays.asList(7, 8, 9), prop.getBySetPos());
		assertEquals(Arrays.asList(1, 2), prop.getByWeekNo());
		assertNull(prop.getWorkweekStarts());
		assertWarnings(15, result.getWarnings());
	}
}
