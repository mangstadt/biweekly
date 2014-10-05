package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import biweekly.ICalVersion;
import biweekly.io.json.JCalValue;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.Geo;

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
 * @author Michael Angstadt
 */
public class GeoScribeTest {
	private final GeoScribe marshaller = new GeoScribe();
	private final Sensei<Geo> sensei = new Sensei<Geo>(marshaller);

	private final Geo withBoth = new Geo(12.34, 56.78);
	private final Geo withLatitude = new Geo(12.34, null);
	private final Geo withLongitude = new Geo(null, 56.78);
	private final Geo withManyDecimals = new Geo(12.3444444444, 56.7777777777);
	private final Geo empty = new Geo(null, null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withBoth).version(ICalVersion.V1_0).run("12.34,56.78");
		sensei.assertWriteText(withBoth).version(ICalVersion.V2_0_DEPRECATED).run("12.34;56.78");
		sensei.assertWriteText(withBoth).version(ICalVersion.V2_0).run("12.34;56.78");

		sensei.assertWriteText(withLatitude).version(ICalVersion.V1_0).run("12.34,0.0");
		sensei.assertWriteText(withLatitude).version(ICalVersion.V2_0_DEPRECATED).run("12.34;0.0");
		sensei.assertWriteText(withLatitude).version(ICalVersion.V2_0).run("12.34;0.0");

		sensei.assertWriteText(withLongitude).version(ICalVersion.V1_0).run("0.0,56.78");
		sensei.assertWriteText(withLongitude).version(ICalVersion.V2_0_DEPRECATED).run("0.0;56.78");
		sensei.assertWriteText(withLongitude).version(ICalVersion.V2_0).run("0.0;56.78");

		sensei.assertWriteText(withManyDecimals).version(ICalVersion.V1_0).run("12.344444,56.777778");
		sensei.assertWriteText(withManyDecimals).version(ICalVersion.V2_0_DEPRECATED).run("12.344444;56.777778");
		sensei.assertWriteText(withManyDecimals).version(ICalVersion.V2_0).run("12.344444;56.777778");

		sensei.assertWriteText(empty).version(ICalVersion.V1_0).run("0.0,0.0");
		sensei.assertWriteText(empty).version(ICalVersion.V2_0_DEPRECATED).run("0.0;0.0");
		sensei.assertWriteText(empty).version(ICalVersion.V2_0).run("0.0;0.0");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("12.34,56.78").versions(ICalVersion.V1_0).run(has(12.34, 56.78));
		sensei.assertParseText("12.34;56.78").versions(ICalVersion.V1_0).cannotParse();

		sensei.assertParseText("12.34;56.78").versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run(has(12.34, 56.78));
		sensei.assertParseText("12.34,56.78").versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).cannotParse();

		sensei.assertParseText("invalid;56.78").cannotParse();
		sensei.assertParseText("12.34;invalid").cannotParse();
		sensei.assertParseText("12.34").cannotParse();
		sensei.assertParseText("").cannotParse();
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withBoth).run("<latitude>12.34</latitude><longitude>56.78</longitude>");
		sensei.assertWriteXml(withLatitude).run("<latitude>12.34</latitude><longitude>0.0</longitude>");
		sensei.assertWriteXml(withLongitude).run("<latitude>0.0</latitude><longitude>56.78</longitude>");
		sensei.assertWriteXml(withManyDecimals).run("<latitude>12.344444</latitude><longitude>56.777778</longitude>");
		sensei.assertWriteXml(empty).run("<latitude>0.0</latitude><longitude>0.0</longitude>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<latitude>12.34</latitude><longitude>56.78</longitude>").run(has(12.34, 56.78));
		sensei.assertParseXml("<latitude>invalid</latitude><longitude>56.78</longitude>").cannotParse();
		sensei.assertParseXml("<latitude>12.34</latitude><longitude>invalid</longitude>").cannotParse();
		sensei.assertParseXml("<latitude>12.34</latitude>").cannotParse();
		sensei.assertParseXml("<longitude>56.78</longitude>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withBoth).run(JCalValue.structured(12.34, 56.78));
		sensei.assertWriteJson(withLatitude).run(JCalValue.structured(12.34, 0.0));
		sensei.assertWriteJson(withLongitude).run(JCalValue.structured(0.0, 56.78));
		sensei.assertWriteJson(withManyDecimals).run(JCalValue.structured(12.3444444444, 56.7777777777));
		sensei.assertWriteJson(empty).run(JCalValue.structured(0.0, 0.0));
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(JCalValue.structured(12.34, 56.78)).run(has(12.34, 56.78));
		sensei.assertParseJson(JCalValue.structured(null, 56.78)).run(has(null, 56.78));
		sensei.assertParseJson(JCalValue.structured(12.34, null)).run(has(12.34, null));
		sensei.assertParseJson(JCalValue.structured(null, null)).run(has(null, null));

		sensei.assertParseJson(JCalValue.structured("invalid", 56.78)).cannotParse();
		sensei.assertParseJson(JCalValue.structured(12.34, "invalid")).cannotParse();
		sensei.assertParseJson("").run(has(null, null));
	}

	private Check<Geo> has(final Double latitude, final Double longitude) {
		return new Check<Geo>() {
			public void check(Geo actual) {
				if (latitude == null) {
					assertNull(actual.getLatitude());
				} else {
					assertEquals(latitude, actual.getLatitude(), 0.001);
				}

				if (longitude == null) {
					assertNull(actual.getLongitude());
				} else {
					assertEquals(longitude, actual.getLongitude(), 0.001);
				}
			}
		};
	}
}
