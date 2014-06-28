package biweekly.property;

import java.util.Arrays;
import java.util.Collection;

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
 * Defines the status of the component that this property belongs to, such as a
 * to-do task being "completed".
 * </p>
 * 
 * <p>
 * <b>Code sample (creating):</b>
 * 
 * <pre class="brush:java">
 * VTodo todo = new VTodo();
 * 
 * Status status = Status.completed();
 * todo.setStatus(status);
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Code sample (retrieving):</b>
 * 
 * <pre class="brush:java">
 * ICalendar ical = ...
 * for (VTodo todo : ical.getTodos()){
 *   Status status = todo.getStatus();
 *   if (action.isCompleted()) {
 * 	   ...
 *   } else if (action.isDraft()){
 *     ...
 *   }
 *   //etc.
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-92">RFC 5545 p.92-3</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.35-6</a>
 */
public class Status extends EnumProperty {
	private static final String TENTATIVE = "TENTATIVE";
	private static final String CONFIRMED = "CONFIRMED";
	private static final String CANCELLED = "CANCELLED";
	private static final String NEEDS_ACTION = "NEEDS-ACTION";
	private static final String COMPLETED = "COMPLETED";
	private static final String IN_PROGRESS = "IN-PROGRESS";
	private static final String DRAFT = "DRAFT";
	private static final String FINAL = "FINAL";

	/**
	 * Creates a status property. Use of this constructor is discouraged and may
	 * put the property in an invalid state. Use one of the static factory
	 * methods instead.
	 * @param status the status (e.g. "TENTATIVE")
	 */
	public Status(String status) {
		super(status);
	}

	/**
	 * Creates a "tentative" status property (only valid for event components).
	 * @return the property
	 */
	public static Status tentative() {
		return create(TENTATIVE);
	}

	/**
	 * Determines if the status is set to "tentative".
	 * @return true if set to "tentative", false if not
	 */
	public boolean isTentative() {
		return is(TENTATIVE);
	}

	/**
	 * Creates a "confirmed" status property (only valid for event components).
	 * @return the property
	 */
	public static Status confirmed() {
		return create(CONFIRMED);
	}

	/**
	 * Determines if the status is set to "confirmed".
	 * @return true if set to "confirmed", false if not
	 */
	public boolean isConfirmed() {
		return is(CONFIRMED);
	}

	/**
	 * Creates a "cancelled" status property (only valid for event, to-do, and
	 * journal components).
	 * @return the property
	 */
	public static Status cancelled() {
		return create(CANCELLED);
	}

	/**
	 * Determines if the status is set to "cancelled".
	 * @return true if set to "cancelled", false if not
	 */
	public boolean isCancelled() {
		return is(CANCELLED);
	}

	/**
	 * Creates a "needs-action" status property (only valid for to-do
	 * components).
	 * @return the property
	 */
	public static Status needsAction() {
		return create(NEEDS_ACTION);
	}

	/**
	 * Determines if the status is set to "needs-action".
	 * @return true if set to "needs-action", false if not
	 */
	public boolean isNeedsAction() {
		return is(NEEDS_ACTION);
	}

	/**
	 * Creates a "completed" status property (only valid for to-do components).
	 * @return the property
	 */
	public static Status completed() {
		return create(COMPLETED);
	}

	/**
	 * Determines if the status is set to "completed".
	 * @return true if set to "completed", false if not
	 */
	public boolean isCompleted() {
		return is(COMPLETED);
	}

	/**
	 * Creates a "in-progress" status property (only valid for to-do
	 * components).
	 * @return the property
	 */
	public static Status inProgress() {
		return create(IN_PROGRESS);
	}

	/**
	 * Determines if the status is set to "in-progress".
	 * @return true if set to "in-progress", false if not
	 */
	public boolean isInProgress() {
		return is(IN_PROGRESS);
	}

	/**
	 * Creates a "draft" status property (only valid for journal components).
	 * @return the property
	 */
	public static Status draft() {
		return create(DRAFT);
	}

	/**
	 * Determines if the status is set to "draft".
	 * @return true if set to "draft", false if not
	 */
	public boolean isDraft() {
		return is(DRAFT);
	}

	/**
	 * Creates a "final" status property (only valid for journal components).
	 * @return the property
	 */
	public static Status final_() {
		return create(FINAL);
	}

	/**
	 * Determines if the status is set to "final".
	 * @return true if set to "final", false if not
	 */
	public boolean isFinal() {
		return is(FINAL);
	}

	private static Status create(String status) {
		return new Status(status);
	}

	@Override
	protected Collection<String> getStandardValues() {
		return Arrays.asList(TENTATIVE, CONFIRMED, CANCELLED, NEEDS_ACTION, COMPLETED, IN_PROGRESS, DRAFT, FINAL);
	}
}
