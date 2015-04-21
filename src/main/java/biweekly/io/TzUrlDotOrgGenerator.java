package biweekly.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import biweekly.component.VTimezone;
import biweekly.io.text.ICalReader;
import biweekly.util.IOUtils;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
 * Downloads {@link VTimezone} components from <a
 * href="http://www.tzurl.org">tzurl.org</a>.
 * @author Michael Angstadt
 */
public class TzUrlDotOrgGenerator implements VTimezoneGenerator {
	private static final Map<URI, VTimezone> cache = Collections.synchronizedMap(new HashMap<URI, VTimezone>());
	private final String baseUrl;

	/**
	 * Creates a new tzurl.org translator.
	 * @param outlook true to generate Outlook-compatible {@link VTimezone}
	 * components, false to use standards-based ones
	 */
	public TzUrlDotOrgGenerator(boolean outlook) {
		baseUrl = "http://www.tzurl.org/zoneinfo" + (outlook ? "-outlook" : "") + "/";
	}

	public VTimezone generate(TimeZone timezone) throws IllegalArgumentException {
		URI uri;
		try {
			uri = new URI(baseUrl + timezone.getID());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}

		VTimezone component = cache.get(uri);
		if (component != null) {
			return component;
		}

		ICalReader reader = null;
		try {
			reader = new ICalReader(uri.toURL().openStream());
			reader.readNext();

			TimezoneInfo tzinfo = reader.getTimezoneInfo();
			component = tzinfo.getComponents().iterator().next();
			cache.put(uri, component);
			return component;
		} catch (FileNotFoundException e) {
			throw notFound(e);
		} catch (NoSuchElementException e) {
			throw notFound(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private IllegalArgumentException notFound(Exception e) {
		return new IllegalArgumentException("Timezone ID not recognized.", e);
	}
}
