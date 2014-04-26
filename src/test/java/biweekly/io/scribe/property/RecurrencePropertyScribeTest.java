package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.assertIntEquals;
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

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.io.json.JCalValue;
import biweekly.io.json.JsonValue;
import biweekly.io.scribe.property.RecurrencePropertyScribe;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.RecurrenceProperty;
import biweekly.util.DefaultTimezoneRule;
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
public class RecurrencePropertyScribeTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	private final RecurrencePropertyMarshallerImpl marshaller = new RecurrencePropertyMarshallerImpl();
	private final Sensei<RecurrenceProperty> sensei = new Sensei<RecurrenceProperty>(marshaller);

	private final Date date;
	{
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DATE, 11);
		date = c.getTime();
	}
	private final String dateStr = "20130611";
	private final String dateStrExt = "2013-06-11";

	private final Date datetime;
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 43);
		c.set(Calendar.SECOND, 2);
		datetime = c.getTime();
	}
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
	private final RecurrenceProperty withUntilDate = new RecurrenceProperty(new Recurrence.Builder(Frequency.WEEKLY).until(date, false).build());
	private final RecurrenceProperty withUntilDateTime = new RecurrenceProperty(new Recurrence.Builder(Frequency.WEEKLY).until(datetime).build());
	private final RecurrenceProperty empty = new RecurrenceProperty(null);

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
	public void writeText_until() {
		sensei.assertWriteText(withUntilDateTime).run("FREQ=WEEKLY;UNTIL=" + dateTimeStr);
		sensei.assertWriteText(withUntilDate).run("FREQ=WEEKLY;UNTIL=" + dateStr);
	}

	@Test
	public void writeText_empty() {
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("FREQ=WEEKLY;COUNT=5;INTERVAL=10;UNTIL=" + dateTimeStr + ";BYSECOND=58,59;BYMINUTE=3,4;BYHOUR=1,2;BYDAY=MO,TU,WE,TH,FR,SA,SU,5FR;BYMONTHDAY=1,2;BYYEARDAY=100,101;BYWEEKNO=1,2;BYMONTH=5,6;BYSETPOS=7,8,9;WKST=TU;X-NAME=one,two;X-RULE=three").run(fullCheck);
	}

	@Test
	public void parseText_invalid_values() {
		sensei.assertParseText("FREQ=W;COUNT=a;INTERVAL=b;UNTIL=invalid;BYSECOND=58,c,59;BYMINUTE=3,d,4;BYHOUR=1,e,2;BYDAY=f,MO,TU,WE,TH,FR,SA,SU,5FR,fFR;BYMONTHDAY=1,g,2;BYYEARDAY=100,h,101;BYWEEKNO=1,w,2;BYMONTH=5,i,6;BYSETPOS=7,8,j,9;WKST=k").warnings(15).run(invalidValuesCheck);
	}

	@Test
	public void parseText_invalid_component() {
		sensei.assertParseText("FREQ=WEEKLY;no equals;COUNT=5").run(new Check<RecurrenceProperty>() {
			public void check(RecurrenceProperty property) {
				Recurrence recur = property.getValue();
				assertEquals(Frequency.WEEKLY, recur.getFrequency());
				assertIntEquals(5, recur.getCount());
				assertNull(recur.getInterval());
				assertNull(recur.getUntil());
				assertEquals(Arrays.asList(), recur.getByMinute());
				assertEquals(Arrays.asList(), recur.getByHour());
				assertEquals(Arrays.asList(), recur.getByDay());
				assertEquals(Arrays.asList(), recur.getByDayPrefixes());
				assertEquals(Arrays.asList(), recur.getByMonthDay());
				assertEquals(Arrays.asList(), recur.getByYearDay());
				assertEquals(Arrays.asList(), recur.getByMonth());
				assertEquals(Arrays.asList(), recur.getBySetPos());
				assertEquals(Arrays.asList(), recur.getByWeekNo());
				assertNull(recur.getWorkweekStarts());

				ListMultimap<String, String> expected = new ListMultimap<String, String>();
				expected.put("NO EQUALS", "");
				assertEquals(expected.getMap(), recur.getXRules());
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

	private static class RecurrencePropertyMarshallerImpl extends RecurrencePropertyScribe<RecurrenceProperty> {
		public RecurrencePropertyMarshallerImpl() {
			super(RecurrenceProperty.class, "RECURRENCE");
		}

		@Override
		protected RecurrenceProperty newInstance(Recurrence recur) {
			return new RecurrenceProperty(recur);
		}
	}

	private final Check<RecurrenceProperty> fullCheck = new Check<RecurrenceProperty>() {
		public void check(RecurrenceProperty property) {
			Recurrence recur = property.getValue();
			assertEquals(Frequency.WEEKLY, recur.getFrequency());
			assertIntEquals(5, recur.getCount());
			assertIntEquals(10, recur.getInterval());
			assertEquals(datetime, recur.getUntil());
			assertEquals(Arrays.asList(3, 4), recur.getByMinute());
			assertEquals(Arrays.asList(1, 2), recur.getByHour());
			assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY), recur.getByDay());
			assertEquals(Arrays.asList(null, null, null, null, null, null, null, Integer.valueOf(5)), recur.getByDayPrefixes());
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
		public void check(RecurrenceProperty property) {
			Recurrence recur = property.getValue();
			assertNull(recur.getFrequency());
			assertNull(recur.getCount());
			assertNull(recur.getInterval());
			assertNull(recur.getUntil());
			assertEquals(Arrays.asList(3, 4), recur.getByMinute());
			assertEquals(Arrays.asList(1, 2), recur.getByHour());
			assertEquals(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY), recur.getByDay());
			assertEquals(Arrays.asList(null, null, null, null, null, null, null, Integer.valueOf(5)), recur.getByDayPrefixes());
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
		public void check(RecurrenceProperty property) {
			Recurrence recur = property.getValue();
			assertNull(recur.getFrequency());
			assertNull(recur.getCount());
			assertNull(recur.getInterval());
			assertNull(recur.getUntil());
			assertEquals(Arrays.asList(), recur.getByMinute());
			assertEquals(Arrays.asList(), recur.getByHour());
			assertEquals(Arrays.asList(), recur.getByDay());
			assertEquals(Arrays.asList(), recur.getByDayPrefixes());
			assertEquals(Arrays.asList(), recur.getByMonthDay());
			assertEquals(Arrays.asList(), recur.getByYearDay());
			assertEquals(Arrays.asList(), recur.getByMonth());
			assertEquals(Arrays.asList(), recur.getBySetPos());
			assertEquals(Arrays.asList(), recur.getByWeekNo());
			assertNull(recur.getWorkweekStarts());
			assertTrue(recur.getXRules().isEmpty());
		}
	};
}
