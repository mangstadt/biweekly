package biweekly.io.scribe.property;

import static biweekly.io.scribe.property.ICalPropertyScribe.date;

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

		StringBuilder sb = new StringBuilder();

		Integer interval = recur.getInterval();
		if (interval == null) {
			interval = 1;
		}

		switch (frequency) {
		case YEARLY:
			if (!recur.getByMonth().isEmpty()) {
				sb.append("YM").append(interval);
				for (Integer month : recur.getByMonth()) {
					sb.append(' ').append(month);
				}
			} else {
				sb.append("YD").append(interval);
				for (Integer day : recur.getByYearDay()) {
					sb.append(' ').append(day);
				}
			}
			break;

		case MONTHLY:
			if (!recur.getByMonthDay().isEmpty()) {
				sb.append("MD").append(interval);
				for (Integer day : recur.getByMonthDay()) {
					sb.append(' ').append(writeVCalInt(day));
				}
			} else {
				sb.append("MP").append(interval);
				for (ByDay byDay : recur.getByDay()) {
					DayOfWeek day = byDay.getDay();
					Integer prefix = byDay.getNum();
					if (prefix == null) {
						prefix = 1;
					}

					sb.append(' ').append(writeVCalInt(prefix)).append(' ').append(day.getAbbr());
				}
			}
			break;

		case WEEKLY:
			sb.append("W").append(interval);
			for (ByDay byDay : recur.getByDay()) {
				sb.append(' ').append(byDay.getDay().getAbbr());
			}
			break;

		case DAILY:
			sb.append("D").append(interval);
			break;

		case HOURLY:
			sb.append("M").append(interval * 60);
			break;

		case MINUTELY:
			sb.append("M").append(interval);
			break;

		default:
			return "";
		}

		writeCountOrUntil(recur, property, sb);

		return sb.toString();
	}

	private void writeCountOrUntil(Recurrence recur, RecurrenceProperty property, StringBuilder sb) {
		Integer count = recur.getCount();
		ICalDate until = recur.getUntil();
		sb.append(' ');

		if (count != null) {
			sb.append('#').append(count);
		} else if (until != null) {
			String dateStr = date(until, property, context).extended(false).write();
			sb.append(dateStr);
		} else {
			sb.append("#0"); //infinite
		}
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
}
