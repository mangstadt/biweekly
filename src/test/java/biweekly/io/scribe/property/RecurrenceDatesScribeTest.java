package biweekly.io.scribe.property;

import static biweekly.ICalDataType.DATE;
import static biweekly.ICalDataType.DATE_TIME;
import static biweekly.ICalDataType.PERIOD;
import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.buildTimezone;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.icalDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.component.Observance;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext;
import biweekly.io.ParseContext.TimezonedDate;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.json.JCalValue;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.RecurrenceDates;
import biweekly.util.Duration;
import biweekly.util.ICalDate;
import biweekly.util.ListMultimap;
import biweekly.util.Period;

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
public class RecurrenceDatesScribeTest extends ScribeTest<RecurrenceDates> {
	private final Date startDate = date(2013, 6, 11);
	private final String startDateStr = "20130611";
	private final String startDateStrExt = "2013-06-11";

	private final Date startDateTime = date(2013, 6, 11, 13, 43, 2);
	private final String startDateTimeStr = startDateStr + "T124302Z";
	private final String startDateTimeStrExt = startDateStrExt + "T12:43:02Z";

	private final Date endDate = startDate;
	private final String endDateStr = startDateStr;
	private final String endDateStrExt = startDateStrExt;

	private final Date endDateTime;
	{
		Calendar c = Calendar.getInstance();
		c.setTime(startDateTime);
		c.add(Calendar.HOUR, 2);
		endDateTime = c.getTime();
	}
	private final String endDateTimeStr = endDateStr + "T144302Z";
	private final String endDateTimeStrExt = endDateStrExt + "T14:43:02Z";

	private final Duration duration = Duration.builder().hours(2).build();
	private final String durationStr = duration.toString();

	private final Period period1 = new Period(startDateTime, endDateTime);
	private final Period period2 = new Period(startDateTime, duration);

	private final RecurrenceDates withMultiplePeriods = new RecurrenceDates();
	{
		withMultiplePeriods.getPeriods().add(period1);
		withMultiplePeriods.getPeriods().add(period2);
	}
	private final RecurrenceDates withMultipleDates = new RecurrenceDates();
	{
		withMultipleDates.getDates().add(new ICalDate(startDate, false));
		withMultipleDates.getDates().add(new ICalDate(endDate, false));
	}
	private final RecurrenceDates withMultipleDateTimes = new RecurrenceDates();
	{
		withMultipleDateTimes.getDates().add(new ICalDate(startDateTime, true));
		withMultipleDateTimes.getDates().add(new ICalDate(endDateTime, true));
	}

	private final RecurrenceDates withSinglePeriod = new RecurrenceDates();
	{
		withSinglePeriod.getPeriods().add(period1);
	}
	private final RecurrenceDates withSingleDate = new RecurrenceDates();
	{
		withSingleDate.getDates().add(new ICalDate(startDate, false));
	}
	private final RecurrenceDates withSingleDateTime = new RecurrenceDates();
	{
		withSingleDateTime.getDates().add(new ICalDate(startDateTime, true));
	}

	private final TimezoneInfo tzinfo;
	{
		tzinfo = new TimezoneInfo();
		VTimezone component = new VTimezone("id");
		TimeZone timezone = buildTimezone(-5, 0);
		timezone.setID("id");
		tzinfo.setDefaultTimezone(new TimezoneAssignment(timezone, component));
	}

	private final RecurrenceDates empty = new RecurrenceDates();

	public RecurrenceDatesScribeTest() {
		super(new RecurrenceDatesScribe());
	}

	@Test
	public void dataType() {
		sensei.assertDataType(withMultiplePeriods).run(PERIOD);
		sensei.assertDataType(withMultipleDates).run(DATE);
		sensei.assertDataType(withMultipleDateTimes).run(DATE_TIME);

		sensei.assertDataType(withSinglePeriod).run(PERIOD);
		sensei.assertDataType(withSingleDate).run(DATE);
		sensei.assertDataType(withSingleDateTime).run(DATE_TIME);

		sensei.assertDataType(empty).run(DATE_TIME);
	}

	@Test
	public void prepareParameters() {
		sensei.assertPrepareParams(withMultiplePeriods).tz(tzinfo).versions(V1_0).run();
		sensei.assertPrepareParams(withMultiplePeriods).tz(tzinfo).versions(V2_0_DEPRECATED, V2_0).expected("TZID", "id").run();

		sensei.assertPrepareParams(withMultipleDates).tz(tzinfo).run();

		sensei.assertPrepareParams(withMultipleDateTimes).tz(tzinfo).versions(V1_0).run();
		sensei.assertPrepareParams(withMultipleDateTimes).tz(tzinfo).versions(V2_0_DEPRECATED, V2_0).expected("TZID", "id").run();

		sensei.assertPrepareParams(empty).tz(tzinfo).run();

		Observance parent = new Observance();
		sensei.assertPrepareParams(withMultiplePeriods).tz(tzinfo).parent(parent).run();
		sensei.assertPrepareParams(withMultipleDateTimes).tz(tzinfo).parent(parent).run();
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withMultiplePeriods).run(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr + "/" + durationStr);
		sensei.assertWriteText(withMultipleDates).run(startDateStr + "," + endDateStr);
		sensei.assertWriteText(withMultipleDateTimes).run(startDateTimeStr + "," + endDateTimeStr);

		sensei.assertWriteText(withSinglePeriod).run(startDateTimeStr + "/" + endDateTimeStr);
		sensei.assertWriteText(withSingleDate).run(startDateStr);
		sensei.assertWriteText(withSingleDateTime).run(startDateTimeStr);

		sensei.assertWriteText(withMultiplePeriods).tz(tzinfo).run("20130611T074302/20130611T094302" + ",20130611T074302/" + durationStr);
		sensei.assertWriteText(withMultipleDates).tz(tzinfo).run("20130611,20130611");
		sensei.assertWriteText(withMultipleDateTimes).tz(tzinfo).run("20130611T074302,20130611T094302");

		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr + "/" + durationStr).dataType(PERIOD).run(is(withMultiplePeriods));
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + "invalid/" + durationStr).dataType(PERIOD).warnings(1).cannotParse(10);
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr + "/invalid").dataType(PERIOD).cannotParse(14);
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr + "/").dataType(PERIOD).cannotParse(14);
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr).dataType(PERIOD).cannotParse(13);

		sensei.assertParseText(startDateStr + "," + endDateStr).dataType(DATE).run(is(withMultipleDates));
		sensei.assertParseText(startDateStr + ",invalid").dataType(DATE).cannotParse(15);

		sensei.assertParseText(startDateTimeStr + "," + endDateTimeStr).dataType(DATE_TIME).run(is(withMultipleDateTimes));
		sensei.assertParseText(startDateTimeStr + ",invalid").dataType(DATE_TIME).cannotParse(15);

		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr).dataType(PERIOD).run(is(withSinglePeriod));
		sensei.assertParseText(startDateStr).dataType(DATE).run(is(withSingleDate));
		sensei.assertParseText(startDateTimeStr).dataType(DATE_TIME).run(is(withSingleDateTime));

		sensei.assertParseText("").run(is(empty));

		//timezone tests
		{
			sensei.assertParseText("20141026T120000/20141026T140000,20141026T120000/" + durationStr).dataType(PERIOD).param("TZID", "id").run(periodWithTimezone(false));
			sensei.assertParseText("20141026T120000/20141026T140000,20141026T150000/" + durationStr).dataType(PERIOD).run(periodWithoutTimezone(false));
			sensei.assertParseText("20141026T120000,20141026T140000").dataType(DATE_TIME).param("TZID", "id").run(dateTimeWithTimezone(false));
			sensei.assertParseText("20141026T120000,20141026T140000").dataType(DATE_TIME).run(dateTimeWithoutTimezone(false));
		}
	}

	@Test
	public void writeXml() {
		//@formatter:off
		sensei.assertWriteXml(withMultiplePeriods).run(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<duration>" + durationStr + "</duration>" +
		"</period>"
		);
		
		sensei.assertWriteXml(withMultipleDates).run(
		"<date>" + startDateStrExt + "</date>" +
		"<date>" + endDateStrExt + "</date>"
		);
		
		sensei.assertWriteXml(withMultipleDateTimes).run(
		"<date-time>" + startDateTimeStrExt + "</date-time>" +
		"<date-time>" + endDateTimeStrExt + "</date-time>"
		);
		
		sensei.assertWriteXml(withSinglePeriod).run(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>"
		);
		
		sensei.assertWriteXml(withSingleDate).run(
		"<date>" + startDateStrExt + "</date>"
		);
		
		sensei.assertWriteXml(withSingleDateTime).run(
		"<date-time>" + startDateTimeStrExt + "</date-time>"
		);

		sensei.assertWriteXml(empty).run("<date-time/>");
		//@formatter:on
	}

	@Test
	public void parseXml() {
		//@formatter:off
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<duration>" + durationStr + "</duration>" +
		"</period>"
		).run(is(withMultiplePeriods));
		
		//invalid start date
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>invalid</start>" +
			"<duration>" + durationStr + "</duration>" +
		"</period>"
		).cannotParse(10);
		
		//invalid duration
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<duration>invalid</duration>" +
		"</period>"
		).cannotParse(12);
		
		//missing start date
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<duration>" + durationStr + "</duration>" +
		"</period>"
		).cannotParse(9);
		
		//missing duration
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
		"</period>"
		).cannotParse(13);
		
		//empty <period> element
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period/>"
		).cannotParse(9);
		
		sensei.assertParseXml(
		"<date>" + startDateStrExt + "</date>" +
		"<date>" + endDateStrExt + "</date>"
		).run(is(withMultipleDates));
		
		//invalid date
		sensei.assertParseXml(
		"<date>" + startDateStrExt + "</date>" +
		"<date>invalid</date>"
		).cannotParse(15);
		
		sensei.assertParseXml(
		"<date-time>" + startDateTimeStrExt + "</date-time>" +
		"<date-time>" + endDateTimeStrExt + "</date-time>"
		).run(is(withMultipleDateTimes));
		
		//invalid date-time
		sensei.assertParseXml(
		"<date-time>" + startDateTimeStrExt + "</date-time>" +
		"<date-time>invalid</date-time>"
		).cannotParse(15);
		
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>"
		).run(is(withSinglePeriod));
		
		sensei.assertParseXml(
		"<date>" + startDateStrExt + "</date>"
		).run(is(withSingleDate));
		
		sensei.assertParseXml(
		"<date-time>" + startDateTimeStrExt + "</date-time>"
		).run(is(withSingleDateTime));
		
		sensei.assertParseXml("").cannotParse(23);
		
		//timezone tests
		{
			sensei.assertParseXml(
			"<period>" + 
				"<start>2014-10-26T12:00:00</start>" + 
				"<end>2014-10-26T14:00:00</end>" + 
			"</period>" +
			"<period>" + 
				"<start>2014-10-26T12:00:00</start>" +
				"<duration>" + durationStr + "</duration>" +
			"</period>"
			).param("TZID", "id").run(periodWithTimezone(true));
			
			sensei.assertParseXml(
			"<period>" + 
				"<start>2014-10-26T12:00:00</start>" + 
				"<end>2014-10-26T14:00:00</end>" + 
			"</period>" +
			"<period>" + 
				"<start>2014-10-26T15:00:00</start>" +
				"<duration>" + durationStr + "</duration>" +
			"</period>"
			).run(periodWithoutTimezone(true));
			
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
		sensei.assertWriteJson(withMultiplePeriods).run(JCalValue.multi(startDateTimeStrExt + "/" + endDateTimeStrExt, startDateTimeStrExt + "/" + durationStr));
		sensei.assertWriteJson(withMultipleDates).run(JCalValue.multi(startDateStrExt, endDateStrExt));
		sensei.assertWriteJson(withMultipleDateTimes).run(JCalValue.multi(startDateTimeStrExt, endDateTimeStrExt));

		sensei.assertWriteJson(withSinglePeriod).run(startDateTimeStrExt + "/" + endDateTimeStrExt);
		sensei.assertWriteJson(withSingleDate).run(startDateStrExt);
		sensei.assertWriteJson(withSingleDateTime).run(startDateTimeStrExt);

		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		//@formatter:off
		sensei.assertParseJson(JCalValue.multi(
			startDateTimeStrExt + "/" + endDateTimeStrExt,
			startDateTimeStrExt + "/" + durationStr
		)).dataType(PERIOD).run(is(withMultiplePeriods));
		
		sensei.assertParseJson(JCalValue.multi(
			startDateTimeStrExt + "/" + endDateTimeStrExt,
			"invalid/" + durationStr
		)).dataType(PERIOD).cannotParse(10);
		
		sensei.assertParseJson(JCalValue.multi(
			startDateTimeStrExt + "/" + endDateTimeStrExt,
			startDateTimeStrExt + "/invalid"
		)).dataType(PERIOD).cannotParse(14);
		
		sensei.assertParseJson(JCalValue.multi(
			startDateTimeStrExt + "/" + endDateTimeStrExt,
			startDateTimeStrExt + "/"
		)).dataType(PERIOD).cannotParse(14);
		
		sensei.assertParseJson(JCalValue.multi(
			startDateTimeStrExt + "/" + endDateTimeStrExt,
			startDateTimeStrExt
		)).dataType(PERIOD).cannotParse(13);

		sensei.assertParseJson(JCalValue.multi(
			startDateStrExt,
			endDateStrExt
		)).dataType(DATE).run(is(withMultipleDates));
		
		sensei.assertParseJson(JCalValue.multi(
			startDateStrExt,
			"invalid"
		)).dataType(DATE).cannotParse(15);

		sensei.assertParseJson(JCalValue.multi(
			startDateTimeStrExt,
			endDateTimeStrExt
		)).dataType(DATE_TIME).run(is(withMultipleDateTimes));
		
		sensei.assertParseJson(JCalValue.multi(
			startDateTimeStrExt,
			"invalid"
		)).dataType(DATE_TIME).cannotParse(15);

		sensei.assertParseJson(JCalValue.multi(
			startDateTimeStrExt + "/" + endDateTimeStrExt
		)).dataType(PERIOD).run(is(withSinglePeriod));
		
		sensei.assertParseJson(JCalValue.multi(
			startDateStrExt
		)).dataType(DATE).run(is(withSingleDate));
		
		sensei.assertParseJson(JCalValue.multi(
			startDateTimeStrExt
		)).dataType(DATE_TIME).run(is(withSingleDateTime));

		//timezone tests
		{
			sensei.assertParseJson(JCalValue.multi(
				"2014-10-26T12:00:00/2014-10-26T14:00:00",
				"2014-10-26T12:00:00/" + durationStr
			)).dataType(PERIOD).param("TZID", "id").run(periodWithTimezone(true));
			sensei.assertParseJson(JCalValue.multi(
				"2014-10-26T12:00:00/2014-10-26T14:00:00",
				"2014-10-26T15:00:00/" + durationStr
			)).dataType(PERIOD).run(periodWithoutTimezone(true));
	
			sensei.assertParseJson(JCalValue.multi(
				"2014-10-26T12:00:00",
				"2014-10-26T14:00:00"
			)).param("TZID", "id").dataType(DATE_TIME).run(dateTimeWithTimezone(true));
			sensei.assertParseJson(JCalValue.multi(
				"2014-10-26T12:00:00",
				"2014-10-26T14:00:00"
			)).dataType(DATE_TIME).run(dateTimeWithoutTimezone(true));
		}
		//@formatter:on
	}

	private Check<RecurrenceDates> is(final RecurrenceDates expected) {
		return new Check<RecurrenceDates>() {
			public void check(RecurrenceDates actual, ParseContext context) {
				assertEquals(expected.getPeriods(), actual.getPeriods());
				assertEquals(expected.getDates(), actual.getDates());
				assertEquals(0, context.getFloatingDates().size());
				assertEquals(0, context.getTimezonedDates().size());
			}
		};
	}

	private Check<RecurrenceDates> periodWithTimezone(final boolean extended) {
		return new Check<RecurrenceDates>() {
			public void check(RecurrenceDates property, ParseContext context) {
				assertEquals(0, context.getFloatingDates().size());

				ListMultimap<String, TimezonedDate> timezonedDates = context.getTimezonedDates();
				assertEquals(1, timezonedDates.keySet().size());
				List<TimezonedDate> dates = timezonedDates.get("id");
				assertEquals(3, dates.size());
				assertTrue(dates.contains(new TimezonedDate(icalDate(2014, 10, 26, 12, 0, 0), property)));
				assertTrue(dates.contains(new TimezonedDate(icalDate(2014, 10, 26, 14, 0, 0), property)));
				assertTrue(dates.contains(new TimezonedDate(icalDate(2014, 10, 26, 12, 0, 0), property)));
			}
		};
	}

	private Check<RecurrenceDates> periodWithoutTimezone(final boolean extended) {
		return new Check<RecurrenceDates>() {
			public void check(RecurrenceDates property, ParseContext context) {
				Collection<TimezonedDate> floating = context.getFloatingDates();
				assertEquals(3, floating.size());
				assertTrue(floating.contains(new TimezonedDate(icalDate(2014, 10, 26, 12, 0, 0), property)));
				assertTrue(floating.contains(new TimezonedDate(icalDate(2014, 10, 26, 14, 0, 0), property)));
				assertTrue(floating.contains(new TimezonedDate(icalDate(2014, 10, 26, 15, 0, 0), property)));

				assertEquals(0, context.getTimezonedDates().size());
			}
		};
	}

	private Check<RecurrenceDates> dateTimeWithTimezone(final boolean extended) {
		return new Check<RecurrenceDates>() {
			public void check(RecurrenceDates property, ParseContext context) {
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

	private Check<RecurrenceDates> dateTimeWithoutTimezone(final boolean extended) {
		return new Check<RecurrenceDates>() {
			public void check(RecurrenceDates property, ParseContext context) {
				Collection<TimezonedDate> floating = context.getFloatingDates();
				assertEquals(2, floating.size());
				assertTrue(floating.contains(new TimezonedDate(icalDate(2014, 10, 26, 12, 0, 0), property)));
				assertTrue(floating.contains(new TimezonedDate(icalDate(2014, 10, 26, 14, 0, 0), property)));

				assertEquals(0, context.getTimezonedDates().size());
			}
		};
	}
}
