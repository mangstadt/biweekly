package biweekly.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TimeZone;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VTimezone;
import biweekly.util.IOUtils;

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
 * Downloads {@link VTimezone} components from tzurl.org.
 * @author Michael Angstadt
 * @see <a href="http://www.tzurl.org">http://www.tzurl.org</a>
 */
public class TzUrlDotOrgTranslator implements TimezoneTranslator {
	private final String baseUrl;

	/**
	 * Creates a new tzurl.org translator.
	 * @param outlook true to generate Outlook-compatible {@link VTimezone}
	 * components, false to use standards-based ones
	 */
	public TzUrlDotOrgTranslator(boolean outlook) {
		baseUrl = "http://www.tzurl.org/zoneinfo" + (outlook ? "-outlook" : "") + "/";
	}

	public VTimezone toICalVTimezone(TimeZone timezone) throws IllegalArgumentException {
		URL url;
		try {
			url = new URL(baseUrl + timezone.getID());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		InputStream response = null;
		try {
			response = url.openStream();
			ICalendar ical = Biweekly.parse(response).first();
			return ical.getTimezones().get(0);
		} catch (FileNotFoundException e) {
			//could not find the timezone
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(response);
		}
	}
}
