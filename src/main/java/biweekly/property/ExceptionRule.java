package biweekly.property;

import java.util.List;

import biweekly.component.ICalComponent;

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
 * <p>
 * Defines an exception to a {@link RecurrenceRule}.
 * </p>
 * <p>
 * Note that this property has been removed from the latest version of the iCal
 * specification. Its use should be avoided.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre class="brush:java">
 * //&quot;bi-weekly&quot;
 * Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).interval(2).build();
 * ExceptionRule exrule = new ExceptionRule(recur);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @rfc 2445 p.114-15
 */
public class ExceptionRule extends RecurrenceProperty {
	/**
	 * Creates a new exception rule property.
	 * @param recur the recurrence rule
	 */
	public ExceptionRule(biweekly.util.Recurrence recur) {
		super(recur);
	}

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		super.validate(components, warnings);

		warnings.add("Property has been removed from the latest iCal specification.  Its use should be avoided.");
	}
}
