package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.json.JCalValue;
import biweekly.io.scribe.property.ExceptionDatesScribe;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.ExceptionDates;
import biweekly.util.DefaultTimezoneRule;

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
public class ExceptionDatesScribeTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	private final ExceptionDatesScribe marshaller = new ExceptionDatesScribe();
	private final Sensei<ExceptionDates> sensei = new Sensei<ExceptionDates>(marshaller);

	private final Date date1;
	{
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DATE, 11);
		date1 = c.getTime();
	}
	private final String date1Str = "20130611";
	private final String date1StrExt = "2013-06-11";

	private final Date datetime1;
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date1);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 43);
		c.set(Calendar.SECOND, 2);
		datetime1 = c.getTime();
	}
	private final String datetime1Str = date1Str + "T124302Z";
	private final String datetime1StrExt = date1StrExt + "T12:43:02Z";

	private final Date date2;
	{
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 2000);
		c.set(Calendar.MONTH, Calendar.NOVEMBER);
		c.set(Calendar.DATE, 2);
		date2 = c.getTime();
	}
	private final String date2Str = "20001102";
	private final String date2StrExt = "2000-11-02";

	private final Date datetime2;
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date2);
		c.set(Calendar.HOUR_OF_DAY, 6);
		c.set(Calendar.MINUTE, 2);
		c.set(Calendar.SECOND, 11);
		datetime2 = c.getTime();
	}
	private final String datetime2Str = date2Str + "T050211Z";
	private final String datetime2StrExt = date2StrExt + "T05:02:11Z";

	private final ExceptionDates withDateTimes = new ExceptionDates(true);
	{
		withDateTimes.addValue(datetime1);
		withDateTimes.addValue(datetime2);
	}
	private final ExceptionDates withDates = new ExceptionDates(false);
	{
		withDates.addValue(date1);
		withDates.addValue(date2);
	}
	private final ExceptionDates empty = new ExceptionDates(true);

	@Test
	public void dataType() {
		sensei.assertDataType(withDates).run(ICalDataType.DATE);
		sensei.assertDataType(withDateTimes).run(ICalDataType.DATE_TIME);
		sensei.assertDataType(empty).run(ICalDataType.DATE_TIME);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withDates).run(date1Str + "," + date2Str);
		sensei.assertWriteText(withDateTimes).run(datetime1Str + "," + datetime2Str);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(date1Str + "," + date2Str).run(has(true, date1, date2));
		sensei.assertParseText(date1Str + "," + date2Str).dataType(ICalDataType.DATE).run(is(withDates));
		sensei.assertParseText(date1Str + "," + date2Str).dataType(ICalDataType.DATE_TIME).run(has(true, date1, date2));

		sensei.assertParseText(datetime1Str + "," + datetime2Str).run(is(withDateTimes));
		sensei.assertParseText(datetime1Str + "," + datetime2Str).dataType(ICalDataType.DATE).run(has(false, datetime1, datetime2));
		sensei.assertParseText(datetime1Str + "," + datetime2Str).dataType(ICalDataType.DATE_TIME).run(is(withDateTimes));

		sensei.assertParseText(datetime1Str + ",invalid").cannotParse();

		sensei.assertParseText("").run(has(true));
		sensei.assertParseText("").dataType(ICalDataType.DATE).run(has(false));
		sensei.assertParseText("").dataType(ICalDataType.DATE_TIME).run(has(true));
	}

	@Test
	public void writeXml_datetime() {
		sensei.assertWriteXml(withDates).run("<date>" + date1StrExt + "</date><date>" + date2StrExt + "</date>");
		sensei.assertWriteXml(withDateTimes).run("<date-time>" + datetime1StrExt + "</date-time><date-time>" + datetime2StrExt + "</date-time>");
		sensei.assertWriteXml(empty).run("");
	}

	@Test
	public void parseXml_datetime() {
		sensei.assertParseXml("<date>" + date1Str + "</date><date>" + date2Str + "</date>").run(is(withDates));
		sensei.assertParseXml("<date-time>" + datetime1Str + "</date-time><date-time>" + datetime2Str + "</date-time>").run(is(withDateTimes));

		//combination of <date> and <date-time> elements
		sensei.assertParseXml("<date-time>" + datetime1Str + "</date-time><date>" + date2Str + "</date>").run(has(true, datetime1, date2));

		sensei.assertParseXml("<date>" + date1Str + "</date><date>invalid</date>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withDates).run(JCalValue.multi(date1StrExt, date2StrExt));
		sensei.assertWriteJson(withDateTimes).run(JCalValue.multi(datetime1StrExt, datetime2StrExt));
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(JCalValue.multi(date1StrExt, date2StrExt)).run(has(true, date1, date2));
		sensei.assertParseJson(JCalValue.multi(date1StrExt, date2StrExt)).dataType(ICalDataType.DATE).run(is(withDates));
		sensei.assertParseJson(JCalValue.multi(date1StrExt, date2StrExt)).dataType(ICalDataType.DATE_TIME).run(has(true, date1, date2));

		sensei.assertParseJson(JCalValue.multi(datetime1StrExt, datetime2StrExt)).run(is(withDateTimes));
		sensei.assertParseJson(JCalValue.multi(datetime1StrExt, datetime2StrExt)).dataType(ICalDataType.DATE).run(has(false, datetime1, datetime2));
		sensei.assertParseJson(JCalValue.multi(datetime1StrExt, datetime2StrExt)).dataType(ICalDataType.DATE_TIME).run(is(withDateTimes));

		sensei.assertParseJson(JCalValue.multi(date1Str, "invalid")).cannotParse();
		sensei.assertParseJson("").cannotParse();

		sensei.assertParseJson(JCalValue.multi()).run(has(true));
		sensei.assertParseJson(JCalValue.multi()).dataType(ICalDataType.DATE).run(has(false));
	}

	private final Check<ExceptionDates> has(final boolean hasTime, final Date... dates) {
		return new Check<ExceptionDates>() {
			public void check(ExceptionDates actual) {
				assertEquals(Arrays.asList(dates), actual.getValues());
				assertEquals(hasTime, actual.hasTime());
			}
		};
	}

	private final Check<ExceptionDates> is(final ExceptionDates expected) {
		return new Check<ExceptionDates>() {
			public void check(ExceptionDates actual) {
				assertEquals(expected.getValues(), actual.getValues());
				assertEquals(expected.hasTime(), actual.hasTime());
			}
		};
	}
}
