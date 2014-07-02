package biweekly.property;

import java.util.Date;

import biweekly.component.VAlarm;
import biweekly.util.Duration;

/**
 * <p>
 * Defines an alarm property that is part of the vCalendar (1.0) standard (such
 * as {@link AudioAlarm}).
 * <p>
 * <p>
 * Classes that extend this class are used internally by this library for
 * parsing purposes. If you are creating a new iCalendar object and need to
 * define an alarm, it is recommended that you use the {@link VAlarm} component
 * to create a new alarm.
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0</a>
 */
public class VCalAlarmProperty extends ICalProperty {
	protected Date start;
	protected Duration snooze;
	protected Integer repeat;

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Duration getSnooze() {
		return snooze;
	}

	public void setSnooze(Duration snooze) {
		this.snooze = snooze;
	}

	public Integer getRepeat() {
		return repeat;
	}

	public void setRepeat(Integer repeat) {
		this.repeat = repeat;
	}
}
