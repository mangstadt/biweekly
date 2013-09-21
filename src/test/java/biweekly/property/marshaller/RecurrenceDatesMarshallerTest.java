package biweekly.property.marshaller;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.json.JCalValue;
import biweekly.property.RecurrenceDates;
import biweekly.property.marshaller.Sensei.Check;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.Duration;
import biweekly.util.Period;

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
public class RecurrenceDatesMarshallerTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	private final RecurrenceDatesMarshaller marshaller = new RecurrenceDatesMarshaller();
	private final Sensei<RecurrenceDates> sensei = new Sensei<RecurrenceDates>(marshaller);

	private final Date startDate;
	{
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DATE, 11);
		startDate = c.getTime();
	}
	private final String startDateStr = "20130611";
	private final String startDateStrExt = "2013-06-11";

	private final Date startDateTime;
	{
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 43);
		c.set(Calendar.SECOND, 2);
		startDateTime = c.getTime();
	}
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

	private final RecurrenceDates withMultiplePeriods = new RecurrenceDates(Arrays.asList(period1, period2));
	private final RecurrenceDates withMultipleDates = new RecurrenceDates(Arrays.asList(startDate, endDate), false);
	private final RecurrenceDates withMultipleDateTimes = new RecurrenceDates(Arrays.asList(startDateTime, endDateTime), true);

	private final RecurrenceDates withSinglePeriod = new RecurrenceDates(Arrays.asList(period1));
	private final RecurrenceDates withSingleDate = new RecurrenceDates(Arrays.asList(startDate), false);
	private final RecurrenceDates withSingleDateTime = new RecurrenceDates(Arrays.asList(startDateTime), true);

	private final RecurrenceDates emptyPeriods = new RecurrenceDates(Arrays.<Period> asList());
	private final RecurrenceDates emptyDates = new RecurrenceDates(Arrays.<Date> asList(), false);
	private final RecurrenceDates emptyDateTimes = new RecurrenceDates(Arrays.<Date> asList(), true);
	private final RecurrenceDates empty = new RecurrenceDates(null);

	@Test
	public void dataType() {
		sensei.assertDataType(withMultiplePeriods).run(ICalDataType.PERIOD);
		sensei.assertDataType(withMultipleDates).run(ICalDataType.DATE);
		sensei.assertDataType(withMultipleDateTimes).run(ICalDataType.DATE_TIME);

		sensei.assertDataType(withSinglePeriod).run(ICalDataType.PERIOD);
		sensei.assertDataType(withSingleDate).run(ICalDataType.DATE);
		sensei.assertDataType(withSingleDateTime).run(ICalDataType.DATE_TIME);

		sensei.assertDataType(emptyPeriods).run(ICalDataType.PERIOD);
		sensei.assertDataType(emptyDates).run(ICalDataType.DATE);
		sensei.assertDataType(emptyDateTimes).run(ICalDataType.DATE_TIME);
		sensei.assertDataType(empty).run(ICalDataType.DATE_TIME);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withMultiplePeriods).run(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr + "/" + durationStr);
		sensei.assertWriteText(withMultipleDates).run(startDateStr + "," + endDateStr);
		sensei.assertWriteText(withMultipleDateTimes).run(startDateTimeStr + "," + endDateTimeStr);

		sensei.assertWriteText(withSinglePeriod).run(startDateTimeStr + "/" + endDateTimeStr);
		sensei.assertWriteText(withSingleDate).run(startDateStr);
		sensei.assertWriteText(withSingleDateTime).run(startDateTimeStr);

		sensei.assertWriteText(emptyPeriods).run("");
		sensei.assertWriteText(emptyDates).run("");
		sensei.assertWriteText(emptyDateTimes).run("");
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr + "/" + durationStr).dataType(ICalDataType.PERIOD).run(is(withMultiplePeriods));
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + "invalid/" + durationStr).dataType(ICalDataType.PERIOD).warnings(1).run(is(withSinglePeriod));
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr + "/invalid").dataType(ICalDataType.PERIOD).warnings(1).run(is(withSinglePeriod));
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr + "/").dataType(ICalDataType.PERIOD).warnings(1).run(is(withSinglePeriod));
		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr + "," + startDateTimeStr).dataType(ICalDataType.PERIOD).warnings(1).run(is(withSinglePeriod));

		sensei.assertParseText(startDateStr + "," + endDateStr).dataType(ICalDataType.DATE).run(is(withMultipleDates));
		sensei.assertParseText(startDateStr + ",invalid").dataType(ICalDataType.DATE).warnings(1).run(is(withSingleDate));

		sensei.assertParseText(startDateTimeStr + "," + endDateTimeStr).dataType(ICalDataType.DATE_TIME).run(is(withMultipleDateTimes));
		sensei.assertParseText(startDateTimeStr + ",invalid").dataType(ICalDataType.DATE_TIME).warnings(1).run(is(withSingleDateTime));

		sensei.assertParseText(startDateTimeStr + "/" + endDateTimeStr).dataType(ICalDataType.PERIOD).run(is(withSinglePeriod));
		sensei.assertParseText(startDateStr).dataType(ICalDataType.DATE).run(is(withSingleDate));
		sensei.assertParseText(startDateTimeStr).dataType(ICalDataType.DATE_TIME).run(is(withSingleDateTime));

		sensei.assertParseText("").dataType(ICalDataType.PERIOD).run(is(emptyPeriods));
		sensei.assertParseText("").dataType(ICalDataType.DATE).run(is(emptyDates));
		sensei.assertParseText("").dataType(ICalDataType.DATE_TIME).run(is(emptyDateTimes));
		sensei.assertParseText("").run(is(emptyDateTimes));
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
		
		sensei.assertWriteXml(emptyPeriods).run("<period/>");
		sensei.assertWriteXml(emptyDates).run("<date/>");
		sensei.assertWriteXml(emptyDateTimes).run("<date-time/>");
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
		).warnings(1).run(is(withSinglePeriod));
		
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
		).warnings(1).run(is(withSinglePeriod));
		
		//missing start date
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<duration>" + durationStr + "</duration>" +
		"</period>"
		).warnings(1).run(is(withSinglePeriod));
		
		//missing duration
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
		"</period>"
		).warnings(1).run(is(withSinglePeriod));
		
		//empty <period> element
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startDateTimeStrExt + "</start>" +
			"<end>" + endDateTimeStrExt + "</end>" +
		"</period>" +
		"<period/>"
		).warnings(1).run(is(withSinglePeriod));
		
		sensei.assertParseXml(
		"<date>" + startDateStrExt + "</date>" +
		"<date>" + endDateStrExt + "</date>"
		).run(is(withMultipleDates));
		
		//invalid date
		sensei.assertParseXml(
		"<date>" + startDateStrExt + "</date>" +
		"<date>invalid</date>"
		).warnings(1).run(is(withSingleDate));
		
		sensei.assertParseXml(
		"<date-time>" + startDateTimeStrExt + "</date-time>" +
		"<date-time>" + endDateTimeStrExt + "</date-time>"
		).run(is(withMultipleDateTimes));
		
		//invalid date-time
		sensei.assertParseXml(
		"<date-time>" + startDateTimeStrExt + "</date-time>" +
		"<date-time>invalid</date-time>"
		).warnings(1).run(is(withSingleDateTime));
		
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
		
		sensei.assertParseXml("").cannotParse();
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

		sensei.assertWriteJson(emptyPeriods).run("");
		sensei.assertWriteJson(emptyDates).run("");
		sensei.assertWriteJson(emptyDateTimes).run("");
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(JCalValue.multi(startDateTimeStrExt + "/" + endDateTimeStrExt, startDateTimeStrExt + "/" + durationStr)).dataType(ICalDataType.PERIOD).run(is(withMultiplePeriods));
		sensei.assertParseJson(JCalValue.multi(startDateTimeStrExt + "/" + endDateTimeStrExt, "invalid/" + durationStr)).dataType(ICalDataType.PERIOD).warnings(1).run(is(withSinglePeriod));
		sensei.assertParseJson(JCalValue.multi(startDateTimeStrExt + "/" + endDateTimeStrExt, startDateTimeStrExt + "/invalid")).dataType(ICalDataType.PERIOD).warnings(1).run(is(withSinglePeriod));
		sensei.assertParseJson(JCalValue.multi(startDateTimeStrExt + "/" + endDateTimeStrExt, startDateTimeStrExt + "/")).dataType(ICalDataType.PERIOD).warnings(1).run(is(withSinglePeriod));
		sensei.assertParseJson(JCalValue.multi(startDateTimeStrExt + "/" + endDateTimeStrExt, startDateTimeStrExt)).dataType(ICalDataType.PERIOD).warnings(1).run(is(withSinglePeriod));

		sensei.assertParseJson(JCalValue.multi(startDateStrExt, endDateStrExt)).dataType(ICalDataType.DATE).run(is(withMultipleDates));
		sensei.assertParseJson(JCalValue.multi(startDateStrExt, "invalid")).dataType(ICalDataType.DATE).warnings(1).run(is(withSingleDate));

		sensei.assertParseJson(JCalValue.multi(startDateTimeStrExt, endDateTimeStrExt)).dataType(ICalDataType.DATE_TIME).run(is(withMultipleDateTimes));
		sensei.assertParseJson(JCalValue.multi(startDateTimeStrExt, "invalid")).dataType(ICalDataType.DATE_TIME).warnings(1).run(is(withSingleDateTime));

		sensei.assertParseJson(JCalValue.multi(startDateTimeStrExt + "/" + endDateTimeStrExt)).dataType(ICalDataType.PERIOD).run(is(withSinglePeriod));
		sensei.assertParseJson(JCalValue.multi(startDateStrExt)).dataType(ICalDataType.DATE).run(is(withSingleDate));
		sensei.assertParseJson(JCalValue.multi(startDateTimeStrExt)).dataType(ICalDataType.DATE_TIME).run(is(withSingleDateTime));

		sensei.assertParseJson("").warnings(1).dataType(ICalDataType.PERIOD).run(is(emptyPeriods));
		sensei.assertParseJson("").warnings(1).dataType(ICalDataType.DATE).run(is(emptyDates));
		sensei.assertParseJson("").warnings(1).dataType(ICalDataType.DATE_TIME).run(is(emptyDateTimes));
	}

	private Check<RecurrenceDates> is(final RecurrenceDates expected) {
		return new Check<RecurrenceDates>() {
			public void check(RecurrenceDates actual) {
				assertEquals(expected.getPeriods(), actual.getPeriods());
				assertEquals(expected.getDates(), actual.getDates());
				assertEquals(expected.hasTime(), actual.hasTime());
			}
		};
	}
}
