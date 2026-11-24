package biweekly.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.mangstadt.vinnie.LaciToken;

import biweekly.ICalVersion;
import biweekly.io.ParseContext;
import biweekly.io.scribe.property.RecurrenceRuleScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.RecurrenceRule;
import biweekly.util.Recurrence;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/124"
 */
class Issue124 {
	@Test
	void test() {
		String input = "INVALID";
		Recurrence recur = parseRecurrence(input);

		Map<LaciToken, List<String>> expectedXRules = new HashMap<>();
		List<String> values = new ArrayList<>();
		values.add("");
		expectedXRules.put(new LaciToken(input), values);

		assertNull(recur.getFrequency());
		assertEquals(expectedXRules, recur.getXRules());
	}

	private Recurrence parseRecurrence(String input) {
		RecurrenceRuleScribe scribe = new RecurrenceRuleScribe();
		ParseContext context = new ParseContext();
		context.setVersion(ICalVersion.V2_0);
		RecurrenceRule rrule = scribe.parseText(input, null, new ICalParameters(), context);
		return rrule.getValue();
	}
}
