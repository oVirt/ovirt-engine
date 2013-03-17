package org.ovirt.engine.core.bll.validator;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.matchers.JUnitMatchers;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

/**
 * Useful matchers to use when testing {@link ValidationResult}s, using the
 * {@link org.junit.Assert#assertThat(Object, Matcher)} (or equivalent) method.<br>
 * These matchers are best used when imported statically.<br>
 * <br>
 * A couple of examples:<br>
 * * To check that <i>validationMethod()</i> returned a <b>valid</b> result:
 *
 * <pre>
 * assertThat(<i>validationMethod()</i>,<b>isValid()</b>);
 * </pre>
 *
 *
 * * To check that <i>validationMethod()</i> <b>fails</b> with an error message
 * <i>VdcBllMessages.EXPECTED_ERROR_MESSAGE</i> (no check for replacements will be done):
 *
 * <pre>
 * assertThat(<i>validationMethod()</i>, <b>failsWith</b>(<i>VdcBllMessages.EXPECTED_ERROR_MESSAGE</i>));
 * </pre>
 *
 * * To check that <i>validationMethod()</i> <b>fails</b> with an error message
 * <i>VdcBllMessages.EXPECTED_ERROR_MESSAGE</i> and the <b>replacements</b> contain a string
 * <i>REPLACEMENT_CONSTANT</i>:
 *
 * <pre>
 * assertThat(<i>validationMethod()</i>,
 *         both(<b>replacements</b>(hasItem(containsString(<i>REPLACEMENT_CONSTANT</i>))))
 *                 .and(<b>failsWith</b>(<i>VdcBllMessages.EXPECTED_ERROR_MESSAGE</i>)));
 * </pre>
 *
 * @see org.junit.Assert#assertThat(Object, Matcher)
 * @see JUnitMatchers
 * @see CoreMatchers
 */
public class ValidationResultMatchers {

    /**
     * @return A matcher matching any {@link ValidationResult} that returns true for {@link ValidationResult#isValid()}.
     */
    public static Matcher<ValidationResult> isValid() {
        return new IsValid();
    }

    /**
     * @param expectedError
     *            The error message expected in {@link ValidationResult#getMessage()}
     * @return A matcher matching any {@link ValidationResult} that is not valid and fails with the given error.
     */
    public static Matcher<ValidationResult> failsWith(VdcBllMessages expectedError) {
        return new Fails(expectedError);
    }

    /**
     * @param matcher
     *            The matcher to match against {@link ValidationResult#getVariableReplacements()}
     * @return A matcher matching any {@link ValidationResult} that it's variable replacements match the given matcher.
     */
    public static Matcher<ValidationResult> replacements(Matcher<Iterable<String>> matcher) {
        return new Replacements(matcher);
    }

    private static class IsValid extends TypeSafeMatcher<ValidationResult> {

        @Override
        public void describeTo(Description description) {
            description.appendText("valid result");
        }

        @Override
        public boolean matchesSafely(ValidationResult item) {
            return item.isValid();
        }
    }

    private static class WithMessage extends TypeSafeMatcher<ValidationResult> {

        private VdcBllMessages expected;

        public WithMessage(VdcBllMessages expected) {
            this.expected = expected;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with message " + expected.name());
        }

        @Override
        public boolean matchesSafely(ValidationResult item) {
            return expected == item.getMessage();
        }
    }

    private static class Replacements extends TypeSafeMatcher<ValidationResult> {

        private Matcher<Iterable<String>> matcher;

        public Replacements(Matcher<Iterable<String>> matcher) {
            this.matcher = matcher;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("the variable replacements is ").appendDescriptionOf(matcher);
        }

        @Override
        public boolean matchesSafely(ValidationResult item) {
            return matcher.matches(item.getVariableReplacements());
        }
    }

    private static class Fails extends TypeSafeMatcher<ValidationResult> {

        private Matcher<ValidationResult> matcher;

        public Fails(VdcBllMessages expected) {
            matcher = JUnitMatchers.both(CoreMatchers.not(isValid())).and(new WithMessage(expected));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a failure ").appendDescriptionOf(matcher);
        }

        @Override
        public boolean matchesSafely(ValidationResult actual) {
            return matcher.matches(actual);
        }
    }
}
