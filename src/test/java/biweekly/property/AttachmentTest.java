package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/*
 Copyright (c) 2013-2020, Michael Angstadt
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
public class AttachmentTest {
	@Test
	public void constructors() throws Exception {
		Attachment property = new Attachment("image/png", "data".getBytes());
		assertEquals("image/png", property.getFormatType());
		assertNull(property.getUri());
		assertArrayEquals("data".getBytes(), property.getData());
		assertNull(property.getContentId());

		property = new Attachment("image/png", "uri");
		assertEquals("image/png", property.getFormatType());
		assertEquals("uri", property.getUri());
		assertNull(property.getData());
		assertNull(property.getContentId());

		File file = new File("pom.xml");
		property = new Attachment("image/png", file);
		assertEquals("image/png", property.getFormatType());
		assertNull(property.getUri());
		assertEquals(file.length(), property.getData().length);
		assertNull(property.getContentId());
	}

	@Test
	public void set_value() {
		Attachment property = new Attachment("image/png", "data".getBytes());

		property.setUri("uri");
		assertEquals("image/png", property.getFormatType());
		assertEquals("uri", property.getUri());
		assertNull(property.getData());
		assertNull(property.getContentId());

		property.setData("data".getBytes());
		assertEquals("image/png", property.getFormatType());
		assertNull(property.getUri());
		assertArrayEquals("data".getBytes(), property.getData());
		assertNull(property.getContentId());

		property.setContentId("contentID");
		assertEquals("image/png", property.getFormatType());
		assertNull(property.getUri());
		assertNull(property.getData());
		assertEquals("contentID", property.getContentId());

		property.setFormatType("image/jpeg");
		assertEquals("image/jpeg", property.getFormatType());
		assertNull(property.getUri());
		assertNull(property.getData());
		assertEquals("contentID", property.getContentId());

		property.setUri("uri");
		assertEquals("image/jpeg", property.getFormatType());
		assertEquals("uri", property.getUri());
		assertNull(property.getData());
		assertNull(property.getContentId());
	}

	@Test
	public void validate() {
		Attachment attach = new Attachment(null, (byte[]) null);
		assertValidate(attach).run(26);

		attach = new Attachment(null, "http://example.com");
		assertValidate(attach).run();

		attach = new Attachment(null, new byte[0]);
		assertValidate(attach).run();

		attach = new Attachment(null, (byte[]) null);
		attach.setContentId("content-id");
		assertValidate(attach).run();
	}

	@Test
	public void toStringValues() {
		Attachment property = new Attachment(null, (String) null);
		assertFalse(property.toStringValues().isEmpty());

		property = new Attachment("image/png", "uri");
		assertFalse(property.toStringValues().isEmpty());

		property = new Attachment("image/png", "data".getBytes());
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Attachment original = new Attachment("image/png", "uri");
		assertCopy(original);

		original = new Attachment("image/png", "data".getBytes());
		assertCopy(original).notSame("getData");

		original = new Attachment("image/png", "uri");
		original.setContentId("contentID");
		assertCopy(original);
	}

	@Test
	public void equals() {
		List<ICalProperty> properties = new ArrayList<ICalProperty>();

		Attachment property = new Attachment(null, (String) null);
		properties.add(property);

		property = new Attachment(null, "uri");
		properties.add(property);

		property = new Attachment("image/png", "uri");
		properties.add(property);

		property = new Attachment("image/png", "uri2");
		properties.add(property);

		property = new Attachment("image/png2", "uri");
		properties.add(property);

		property = new Attachment(null, "data".getBytes());
		properties.add(property);

		property = new Attachment("image/png", "data".getBytes());
		properties.add(property);

		property = new Attachment("image/png", "data2".getBytes());
		properties.add(property);

		property = new Attachment("image/png2", "data".getBytes());
		properties.add(property);

		property = new Attachment(null, (String) null);
		property.setContentId("contentID");
		properties.add(property);

		property = new Attachment("image/png", (String) null);
		property.setContentId("contentID");
		properties.add(property);

		property = new Attachment("image/png", (String) null);
		property.setContentId("contentID2");
		properties.add(property);

		property = new Attachment("image/png2", (String) null);
		property.setContentId("contentID");
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off

		assertEqualsMethod(Attachment.class, "image/png", "uri")
		.constructor(new Class<?>[]{String.class, String.class}, null, null).test()
		.constructor(new Class<?>[]{String.class, String.class}, null, "uri").test()
		.constructor("image/png", "uri")
			.test()
			.method("setContentId", "contentId")
		.constructor(new Class<?>[]{String.class, byte[].class}, null, "data".getBytes()).test()
		.constructor("image/png", "data".getBytes())
			.test()
			.method("setContentId", "contentId");
		//@formatter:on
	}
}
