package biweekly.io.xml;

import org.xml.sax.SAXException;

/**
 * Thrown from {@link XCalListener#icalRead(ezvcard.VCard, java.util.List)
 * XCalListener.vcardRead()} to signal that the xCard reader should stop parsing
 * vCards.
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class StopReadingException extends SAXException {
	//empty
}
