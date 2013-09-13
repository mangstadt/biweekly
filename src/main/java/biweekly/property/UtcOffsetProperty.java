package biweekly.property;

import java.util.List;

import biweekly.component.ICalComponent;
import biweekly.util.UtcOffset;

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
 * Represents a property whose value is a timezone offset.
 * @author Michael Angstadt
 */
public class UtcOffsetProperty extends ICalProperty {
	protected UtcOffset offset;

	public UtcOffsetProperty(int hourOffset, int minuteOffset) {
		this(new UtcOffset(hourOffset, minuteOffset));
	}

	public UtcOffsetProperty(UtcOffset offset) {
		this.offset = offset;
	}

	public int getHourOffset() {
		return (offset == null) ? null : offset.getHour();
	}

	public int getMinuteOffset() {
		return (offset == null) ? null : offset.getMinute();
	}

	public UtcOffset getOffset() {
		return offset;
	}

	public void setOffset(int hourOffset, int minuteOffset) {
		setOffset(new UtcOffset(hourOffset, minuteOffset));
	}

	public void setOffset(UtcOffset offset) {
		this.offset = offset;
	}

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		if (offset == null) {
			warnings.add("Value is null.");
		}
		if (offset != null && (offset.getMinute() < 0 || offset.getMinute() > 59)) {
			warnings.add("Minute offset must be between 0 and 59 inclusive.");
		}
	}
}
