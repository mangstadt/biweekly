package biweekly.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

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
 * @author Michael Angstadt
 */
public class Base64Test {
	private final byte data[];
	{
		try {
			data = IOUtils.toByteArray(getClass().getResourceAsStream("base64.jpg"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private final String dataBase64 = org.apache.commons.codec.binary.Base64.encodeBase64String(data);

	@Test
	public void encode() throws Throwable {
		String actual = Base64.encode(data);
		assertEquals(dataBase64, actual);
	}

	@Test
	public void decode() throws Throwable {
		byte actual[] = Base64.decode(dataBase64);
		assertArrayEquals(data, actual);
	}

	@Test
	public void round_trip() throws Throwable {
		assertArrayEquals(data, Base64.decode(Base64.encode(data)));
	}
}
