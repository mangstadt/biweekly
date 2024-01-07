package biweekly.io.scribe.property;

import static biweekly.ICalDataType.DATE;
import static biweekly.ICalDataType.DATE_TIME;
import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.icalDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import biweekly.io.ParseContext;
import biweekly.io.ParseContext.TimezonedDate;
import biweekly.io.json.JCalValue;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.ExceptionDates;
import biweekly.util.ICalDate;
import biweekly.util.ListMultimap;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
public class ExceptionDatesScribeTest extends ScribeTest<ExceptionDates> {
	private final ICalDate date1 = new ICalDate(date(2013, 6, 11), false);
	private final String date1Str = "20130611";
	private final String date1StrExt = "2013-06-11";

	private final ICalDate datetime1 = new ICalDate(date(2013, 6, 11, 13, 43, 2), true);
	private final String datetime1Str = date1Str + "T124302Z";
	private final String datetime1StrExt = date1StrExt + "T12:43:02Z";

	private final ICalDate date2 = new ICalDate(date(2000, 11, 2), false);
	private final String date2Str = "20001102";
	private final String date2StrExt = "2000-11-02";

	private final ICalDate datetime2 = new ICalDate(date(2000, 11, 2, 6, 2, 11), true);
	private final String datetime2Str = date2Str + "T050211Z";
	private final String datetime2StrExt = date2StrExt + "T05:02:11Z";

	private final ExceptionDates withDateTimes = new ExceptionDates();
	{
		withDateTimes.getValues().add(datetime1);
		withDateTimes.getValues().add(datetime2);
	}
	private final ExceptionDates withDates = new ExceptionDates();
	{
		withDates.getValues().add(date1);
		withDates.getValues().add(date2);
	}
	private final ExceptionDates empty = new ExceptionDates();

	public ExceptionDatesScribeTest() {
		super(new ExceptionDatesScribe());
	}

	@Test
	public void dataType() {
		sensei.assertDataType(withDates).run(DATE);
		sensei.assertDataType(withDateTimes).run(DATE_TIME);
		sensei.assertDataType(empty).run(DATE_TIME);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withDates).version(V1_0).run(date1Str + ";" + date2Str);
		sensei.assertWriteText(withDates).version(V2_0_DEPRECATED).run(date1Str + "," + date2Str);
		sensei.assertWriteText(withDates).version(V2_0).run(date1Str + "," + date2Str);

		sensei.assertWriteText(withDateTimes).version(V1_0).run(datetime1Str + ";" + datetime2Str);
		sensei.assertWriteText(withDateTimes).version(V2_0_DEPRECATED).run(datetime1Str + "," + datetime2Str);
		sensei.assertWriteText(withDateTimes).version(V2_0).run(datetime1Str + "," + datetime2Str);

		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(date1Str + ";" + date2Str).versions(V1_0).run(has(new ICalDate(date1, true), new ICalDate(date2, true)));
		sensei.assertParseText(date1Str + "," + date2Str).versions(V2_0_DEPRECATED, V2_0).run(has(new ICalDate(date1, true), new ICalDate(date2, true)));

		sensei.assertParseText(date1Str + ";" + date2Str).versions(V1_0).dataType(DATE).run(is(withDates));
		sensei.assertParseText(date1Str + "," + date2Str).versions(V2_0_DEPRECATED, V2_0).dataType(DATE).run(is(withDates));

		sensei.assertParseText(date1Str + ";" + date2Str).versions(V1_0).dataType(DATE_TIME).run(has(new ICalDate(date1, true), new ICalDate(date2, true)));
		sensei.assertParseText(date1Str + "," + date2Str).versions(V2_0_DEPRECATED, V2_0).dataType(DATE_TIME).run(has(new ICalDate(date1, true), new ICalDate(date2, true)));

		sensei.assertParseText(datetime1Str + ";" + datetime2Str).versions(V1_0).run(is(withDateTimes));
		sensei.assertParseText(datetime1Str + "," + datetime2Str).versions(V2_0_DEPRECATED, V2_0).run(is(withDateTimes));

		sensei.assertParseText(datetime1Str + ";" + datetime2Str).versions(V1_0).dataType(DATE).run(has(new ICalDate(datetime1, false), new ICalDate(datetime2, false)));
		sensei.assertParseText(datetime1Str + "," + datetime2Str).versions(V2_0_DEPRECATED, V2_0).dataType(DATE).run(has(new ICalDate(datetime1, false), new ICalDate(datetime2, false)));

		sensei.assertParseText(datetime1Str + ";" + datetime2Str).versions(V1_0).dataType(DATE_TIME).run(is(withDateTimes));
		sensei.assertParseText(datetime1Str + "," + datetime2Str).versions(V2_0_DEPRECATED, V2_0).dataType(DATE_TIME).run(is(withDateTimes));

		sensei.assertParseText(datetime1Str + ";invalid").versions(V1_0).cannotParse(19);
		sensei.assertParseText(datetime1Str + ",invalid").versions(V2_0_DEPRECATED, V2_0).cannotParse(19);

		sensei.assertParseText("").run(has());
		sensei.assertParseText("").dataType(DATE).run(has());
		sensei.assertParseText("").dataType(DATE_TIME).run(has());

		//timezone tests
		{
			sensei.assertParseText("20141026T120000;20141026T140000").versions(V1_0).dataType(DATE_TIME).param("TZID", "id").run(dateTimeWithTimezone(false));
			sensei.assertParseText("20141026T120000,20141026T140000").versions(V2_0_DEPRECATED, V2_0).dataType(DATE_TIME).param("TZID", "id").run(dateTimeWithTimezone(false));

			sensei.assertParseText("20141026T120000;20141026T140000").versions(V1_0).dataType(DATE_TIME).run(dateTimeWithoutTimezone(false));
			sensei.assertParseText("20141026T120000,20141026T140000").versions(V2_0_DEPRECATED, V2_0).dataType(DATE_TIME).run(dateTimeWithoutTimezone(false));
		}
	}

	@Test
	public void writeXml_datetime() {
		sensei.assertWriteXml(withDates).run("<date>" + date1StrExt + "</date><date>" + date2StrExt + "</date>");
		sensei.assertWriteXml(withDateTimes).run("<date-time>" + datetime1StrExt + "</date-time><date-time>" + datetime2StrExt + "</date-time>");
		sensei.assertWriteXml(empty).run("<date-time/>");
	}

	@Test
	public void parseXml_datetime() {
		//@formatter:off
		sensei.assertParseXml(
		"<date>" + date1Str + "</date>" +
		"<date>" + date2Str + "</date>"
		).run(is(withDates));
		
		sensei.assertParseXml(
		"<date-time>" + datetime1Str + "</date-time>" +
		"<date-time>" + datetime2Str + "</date-time>"
		).run(is(withDateTimes));

		//combination of <date> and <date-time> elements
		sensei.assertParseXml("<date-time>" + datetime1Str + "</date-time>" +
		"<date>" + date2Str + "</date>"
		).run(has(datetime1, date2));

		sensei.assertParseXml(
		"<date>" + date1Str + "</date>" + 
		"<date>invalid</date>")
		.cannotParse(19);
		sensei.assertParseXml("").cannotParse(23);
		
		//timezone tests
		{
			sensei.assertParseXml(
			"<date-time>2014-10-26T12:00:00</date-time>" +
			"<date-time>2014-10-26T14:00:00</date-time>"
			).param("TZID", "id").run(dateTimeWithTimezone(true));
			
			sensei.assertParseXml(
			"<date-time>2014-10-26T12:00:00</date-time>" +
			"<date-time>2014-10-26T14:00:00</date-time>"
			).run(dateTimeWithoutTimezone(true));
		}
		//@formatter:on
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withDates).run(JCalValue.multi(date1StrExt, date2StrExt));
		sensei.assertWriteJson(withDateTimes).run(JCalValue.multi(datetime1StrExt, datetime2StrExt));
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(JCalValue.multi(date1StrExt, date2StrExt)).run(has(new ICalDate(date1, true), new ICalDate(date2, true)));
		sensei.assertParseJson(JCalValue.multi(date1StrExt, date2StrExt)).dataType(DATE).run(is(withDates));
		sensei.assertParseJson(JCalValue.multi(date1StrExt, date2StrExt)).dataType(DATE_TIME).run(has(new ICalDate(date1, true), new ICalDate(date2, true)));

		sensei.assertParseJson(JCalValue.multi(datetime1StrExt, datetime2StrExt)).run(is(withDateTimes));
		sensei.assertParseJson(JCalValue.multi(datetime1StrExt, datetime2StrExt)).dataType(DATE).run(has(new ICalDate(datetime1, false), new ICalDate(datetime2, false)));
		sensei.assertParseJson(JCalValue.multi(datetime1StrExt, datetime2StrExt)).dataType(DATE_TIME).run(is(withDateTimes));

		sensei.assertParseJson(JCalValue.multi(date1Str, "invalid")).cannotParse(19);
		sensei.assertParseJson("").cannotParse(19);

		sensei.assertParseJson(JCalValue.multi()).run(has());
		sensei.assertParseJson(JCalValue.multi()).dataType(DATE).run(has());

		//timezone tests
		{
			sensei.assertParseJson(JCalValue.multi("2014-10-26T12:00:00", "2014-10-26T14:00:00")).param("TZID", "id").dataType(DATE_TIME).run(dateTimeWithTimezone(true));
			sensei.assertParseJson(JCalValue.multi("2014-10-26T12:00:00", "2014-10-26T14:00:00")).dataType(DATE_TIME).run(dateTimeWithoutTimezone(true));
		}
	}

	private final Check<ExceptionDates> has(final ICalDate... dates) {
		return new Check<ExceptionDates>() {
			public void check(ExceptionDates actual, ParseContext context) {
				assertEquals(Arrays.asList(dates), actual.getValues());
			}
		};
	}

	private final Check<ExceptionDates> is(final ExceptionDates expected) {
		return new Check<ExceptionDates>() {
			public void check(ExceptionDates actual, ParseContext context) {
				assertEquals(expected.getValues(), actual.getValues());
			}
		};
	}

	private Check<ExceptionDates> dateTimeWithTimezone(final boolean extended) {
		return new Check<ExceptionDates>() {
			public void check(ExceptionDates property, ParseContext context) {
				assertEquals(0, context.getFloatingDates().size());

				ListMultimap<String, TimezonedDate> timezonedDates = context.getTimezonedDates();
				assertEquals(1, timezonedDates.keySet().size());
				List<TimezonedDate> dates = context.getTimezonedDates().get("id");
				assertEquals(2, dates.size());
				assertTrue(dates.contains(new TimezonedDate(icalDate(2014, 10, 26, 12, 0, 0), property)));
				assertTrue(dates.contains(new TimezonedDate(icalDate(2014, 10, 26, 14, 0, 0), property)));
			}
		};
	}

	private Check<ExceptionDates> dateTimeWithoutTimezone(final boolean extended) {
		return new Check<ExceptionDates>() {
			public void check(ExceptionDates property, ParseContext context) {
				Collection<TimezonedDate> floating = context.getFloatingDates();
				assertEquals(2, floating.size());
				assertTrue(floating.contains(new TimezonedDate(icalDate(2014, 10, 26, 12, 0, 0), property)));
				assertTrue(floating.contains(new TimezonedDate(icalDate(2014, 10, 26, 14, 0, 0), property)));

				assertEquals(0, context.getTimezonedDates().size());
			}
		};
	}
}
