package biweekly.util.com.google.ical.iter;

/**
 * <p>
 * A marker for {@link Generator}s that generate exactly one value per outer
 * cycle.
 * </p>
 * <p>
 * For example, {@code BYHOUR=3} generates exactly one hour per day and
 * {@code BYMONTHDAY=12} generates exactly one day per month, but
 * {@code BYHOUR=3,6} does not. Nor does {@code BYMONTHDAY=31}.
 * </p>
 */
abstract class SingleValueGenerator extends Generator {
  /**
   * Gets the single value that this generator generates.
   * @return the value
   */
  abstract int getValue();
}
