package biweekly.io.scribe.property;

import java.util.Arrays;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.property.ProcedureAlarm;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
 * Marshals {@link ProcedureAlarm} properties.
 * @author Michael Angstadt
 */
public class ProcedureAlarmScribe extends VCalAlarmPropertyScribe<ProcedureAlarm> {
	public ProcedureAlarmScribe() {
		super(ProcedureAlarm.class, "PALARM", ICalDataType.TEXT);
	}

	@Override
	protected List<String> writeData(ProcedureAlarm property) {
		String path = property.getPath();
		if (path != null) {
			return Arrays.asList(path);
		}

		return Arrays.asList();
	}

	@Override
	protected ProcedureAlarm create(ICalDataType dataType, SemiStructuredIterator it) {
		return new ProcedureAlarm(it.next());
	}
}
