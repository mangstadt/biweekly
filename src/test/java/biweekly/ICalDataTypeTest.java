package biweekly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

public class ICalDataTypeTest {
	@Test
	public void get() {
		assertTrue(ICalDataType.TEXT == ICalDataType.get("tExT"));

		ICalDataType test = ICalDataType.get("test");
		ICalDataType test2 = ICalDataType.get("tEsT");
		assertEquals("test", test2.getName());
		assertTrue(test == test2);
	}

	@Test
	public void find() {
		assertTrue(ICalDataType.TEXT == ICalDataType.find("tExT"));

		//find() ignores runtime-defined objects
		ICalDataType.get("test");
		assertNull(ICalDataType.find("test"));
	}

	@Test
	public void all() {
		ICalDataType.get("test"); //all() ignores runtime-defined objects
		Collection<ICalDataType> all = ICalDataType.all();

		assertEquals(15, all.size());
		assertTrue(all.contains(ICalDataType.BINARY));
		assertTrue(all.contains(ICalDataType.BOOLEAN));
		assertTrue(all.contains(ICalDataType.CAL_ADDRESS));
		assertTrue(all.contains(ICalDataType.CONTENT_ID));
		assertTrue(all.contains(ICalDataType.DATE));
		assertTrue(all.contains(ICalDataType.DATE_TIME));
		assertTrue(all.contains(ICalDataType.DURATION));
		assertTrue(all.contains(ICalDataType.FLOAT));
		assertTrue(all.contains(ICalDataType.INTEGER));
		assertTrue(all.contains(ICalDataType.PERIOD));
		assertTrue(all.contains(ICalDataType.RECUR));
		assertTrue(all.contains(ICalDataType.TEXT));
		assertTrue(all.contains(ICalDataType.TIME));
		assertTrue(all.contains(ICalDataType.URI));
		assertTrue(all.contains(ICalDataType.UTC_OFFSET));
	}

	@Test
	public void contentId() {
		assertEquals(ICalDataType.CONTENT_ID, ICalDataType.find("CID"));
		assertEquals(ICalDataType.CONTENT_ID, ICalDataType.get("CID"));
	}

	@Test
	public void uri() {
		assertEquals("URI", ICalDataType.URI.getName());
		assertEquals("URL", ICalDataType.URI.getName(ICalVersion.V1_0));
		assertEquals("URI", ICalDataType.URI.getName(ICalVersion.V2_0));
	}
}
