package biweekly.io.scribe.property;

import static biweekly.io.scribe.property.ICalPropertyScribe.date;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import biweekly.io.WriteContext;
import biweekly.property.RecurrenceProperty;
import biweekly.util.ByDay;
import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;

/**
 * Writes iCal 1.0 RRULE values.
 * @author Michael Angstadt
 */
class RecurrenceWriterV1 {
	private final RecurrenceProperty property;
	private final WriteContext context;

	public RecurrenceWriterV1(RecurrenceProperty property, WriteContext context) {
		this.property = property;
		this.context = context;
	}

	public String write() {
		Recurrence recur = property.getValue();
		Frequency frequency = recur.getFrequency();
		if (frequency == null) {
			return "";
		}

		Integer interval = recur.getInterval();
		if (interval == null) {
			interval = 1;
		}

		String prefix;
		Stream<String> values;

		switch (frequency) {
		case YEARLY:
			if (!recur.getByMonth().isEmpty()) {
				prefix = "YM" + interval;
				values = recur.getByMonth().stream().map(Object::toString);
			} else {
				prefix = "YD" + interval;
				values = recur.getByYearDay().stream().map(Object::toString);
			}
			break;

		case MONTHLY:
			if (!recur.getByMonthDay().isEmpty()) {
				prefix = "MD" + interval;
				values = recur.getByMonthDay().stream().map(this::writeVCalInt);
			} else {
				prefix = "MP" + interval;
				values = recur.getByDay().stream().map(this::writeByDay);
			}
			break;

		case WEEKLY:
			prefix = "W" + interval;
			values = recur.getByDay().stream().map(ByDay::getDay).map(DayOfWeek::getAbbr);
			break;

		case DAILY:
			prefix = "D" + interval;
			values = Stream.empty();
			break;

		case HOURLY:
			prefix = "M" + (interval * 60);
			values = Stream.empty();
			break;

		case MINUTELY:
			prefix = "M" + interval;
			values = Stream.empty();
			break;

		default:
			return "";
		}

		String suffix = writeCountOrUntil(recur, property);

		return Stream.concat(Stream.of(prefix), Stream.concat(values, Stream.of(suffix))).collect(Collectors.joining(" "));
	}

	private String writeCountOrUntil(Recurrence recur, RecurrenceProperty property) {
		Integer count = recur.getCount();
		ICalDate until = recur.getUntil();

		if (count != null) {
			return "#" + count;
		}
		if (until != null) {
			return date(until, property, context).extended(false).write();
		}
		return "#0"; //infinite
	}

	private String writeVCalInt(Integer value) {
		if (value > 0) {
			return value + "+";
		}

		if (value < 0) {
			return Math.abs(value) + "-";
		}

		return value.toString();
	}

	private String writeByDay(ByDay byDay) {
		DayOfWeek day = byDay.getDay();
		Integer prefix = byDay.getNum();
		if (prefix == null) {
			prefix = 1;
		}
		return writeVCalInt(prefix) + " " + day.getAbbr();
	}
}
