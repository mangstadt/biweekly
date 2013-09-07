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
 * Defines whether an event is visible to free/busy time searches. If an event
 * does not have this property, the event should be considered visible
 * ("opaque").
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre>
 * //creating a new property
 * Transparency transp = Transparency.opaque();
 * 
 * if (transp.isOpaque()) {
 *   //its value is "OPAQUE"
 * }
 * 
 * //usage in a VEVENT component
 * VEvent event = ...
 * event.setTransparency(true); //hidden from searches ("TRANSPARENT")
 * event.setTransparency(false); //visible to searches ("OPAQUE")
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @rfc 5545 p.101-2
 */
public class Transparency extends EnumProperty {
	private static final String OPAQUE = "OPAQUE";
	private static final String TRANSPARENT = "TRANSPARENT";

	/**
	 * Creates a new transparency property.
	 * @param value the value
	 */
	public Transparency(String value) {
		super(value);
	}

	/**
	 * Creates a property that marks the event as being visible to free/busy
	 * time searches.
	 * @return the property
	 */
	public static Transparency opaque() {
		return create(OPAQUE);
	}

	/**
	 * Determines if the event is visible to free/busy time searches.
	 * @return true if it's visible, false if not
	 */
	public boolean isOpaque() {
		return is(OPAQUE);
	}

	/**
	 * Creates a property that marks the event as being hidden from free/busy
	 * time searches.
	 * @return the property
	 */
	public static Transparency transparent() {
		return create(TRANSPARENT);
	}

	/**
	 * Determines if the event is hidden from free/busy time searches.
	 * @return true if it's hidden, false if not
	 */
	public boolean isTransparent() {
		return is(TRANSPARENT);
	}

	private static Transparency create(String value) {
		return new Transparency(value);
	}

	@Override
	protected Collection<String> getStandardValues() {
		return Arrays.asList(OPAQUE, TRANSPARENT);
	}
}
