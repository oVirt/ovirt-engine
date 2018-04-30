package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.not;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * Useful matchers to use when testing {@link ValidationResult}s, using the
 * {@link org.hamcrest.MatcherAssert#assertThat(Object, Matcher)} (or equivalent) method.<br>
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
 * <i>EngineMessage.EXPECTED_ERROR_MESSAGE</i> (no check for replacements will be done):
 *
 * <pre>
 * assertThat(<i>validationMethod()</i>, <b>failsWith</b>(<i>EngineMessage.EXPECTED_ERROR_MESSAGE</i>));
 * </pre>
 *
 * * To check that <i>validationMethod()</i> <b>fails</b> with an error message
 * <i>EngineMessage.EXPECTED_ERROR_MESSAGE</i> and the <b>replacements</b> contain a string
 * <i>REPLACEMENT_CONSTANT</i>:
 *
 * <pre>
 * assertThat(<i>validationMethod()</i>,
 *         both(<b>replacements</b>(hasItem(containsString(<i>REPLACEMENT_CONSTANT</i>))))
 *                 .and(<b>failsWith</b>(<i>EngineMessage.EXPECTED_ERROR_MESSAGE</i>)));
 * </pre>
 *
 * @see org.hamcrest.MatcherAssert#assertThat(Object, Matcher)
 * @see org.hamcrest.CoreMatchers
 */
public class ValidationResultMatchers {

    //do not instantiate me.
    private ValidationResultMatchers() {
    }

    /**
     * @return A matcher matching any {@link ValidationResult} that returns true for {@link ValidationResult#isValid()}.
     */
    public static Matcher<ValidationResult> isValid() {
        return new IsValid();
    }

    /**
     * @param expectedError
     *            The error message expected in {@link ValidationResult#getMessages()}
     * @return A matcher matching any {@link ValidationResult} that is not valid and fails with the given error.
     */
    public static Matcher<ValidationResult> failsWith(EngineMessage expectedError) {
        return new Fails(expectedError);
    }

    public static Matcher<ValidationResult> failsWith(EngineMessage expectedError, String... variableReplacements) {
        return new Fails(expectedError, variableReplacements);
    }

    public static Matcher<ValidationResult> failsWith(EngineMessage expectedError,
        Collection<String> variableReplacements) {
        return new Fails(expectedError, variableReplacements.toArray(new String[variableReplacements.size()]));
    }

    public static Matcher<ValidationResult> failsWith(List<EngineMessage> expectedErrors,
            Collection<String> variableReplacements) {
        return new Fails(expectedErrors, variableReplacements.toArray(new String[variableReplacements.size()]));
    }

    public static Matcher<ValidationResult> failsWith(ValidationResult validationResult) {
        if (ValidationResult.VALID.equals(validationResult)) {
            throw new IllegalArgumentException("Illegal matcher usage: you cannot pass ValidationResult.VALID here.");
        }
        return failsWith(validationResult.getMessages(), validationResult.getVariableReplacements());
    }

    /**
     * @param matcher
     *            The matcher to match against {@link ValidationResult#getVariableReplacements()}
     * @return A matcher matching any {@link ValidationResult} that it's variable replacements match the given matcher.
     */
    public static Matcher<ValidationResult> replacements(Matcher<Iterable<? super String>> matcher) {
        return new Replacements(matcher);
    }

    public static Matcher<ValidationResult> hasVariableReplacements(String... variableReplacements) {
        return new HasVariableReplacements(variableReplacements);
    }

    private static class IsValid extends TypeSafeMatcher<ValidationResult> {

        @Override
        public void describeTo(Description description) {
            description.appendText("valid ValidationResult");
        }

        @Override
        public boolean matchesSafely(ValidationResult item) {
            return item.isValid();
        }
    }

    private static class WithMessages extends TypeSafeMatcher<ValidationResult> {

        private List<EngineMessage> expected;

        public WithMessages(List<EngineMessage> expected) {
            this.expected = expected;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("messages \"" +
                    expected.stream().map(m -> m.name()).collect(Collectors.joining()) + "\"");
        }

        @Override
        public boolean matchesSafely(ValidationResult item) {
            return CollectionUtils.isEqualCollection(expected, item.getMessages());
        }
    }

    private static class Replacements extends TypeSafeMatcher<ValidationResult> {

        private Matcher<Iterable<? super String>> matcher;

        public Replacements(Matcher<Iterable<? super String>> matcher) {
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

    private static class HasVariableReplacements extends TypeSafeMatcher<ValidationResult> {

        private final String[] variableReplacements;

        public HasVariableReplacements(String... variableReplacements) {
            this.variableReplacements = variableReplacements;
        }

        @Override
        protected boolean matchesSafely(ValidationResult item) {
            final List<String> vr = item.getVariableReplacements();
            return variableReplacements.length == vr.size() && vr.containsAll(Arrays.asList(variableReplacements));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("ValidationResult containing exactly these variable replacements: " + Arrays.toString(variableReplacements));
        }
    }

    private static class Fails extends TypeSafeMatcher<ValidationResult> {

        private Matcher<ValidationResult> matcher;

        public Fails(EngineMessage expected) {
            matcher = both(not(isValid())).and(new WithMessages(Collections.singletonList(expected)));
        }

        public Fails(EngineMessage expected, String... variableReplacements) {
            this(Collections.singletonList(expected), variableReplacements);
        }

        public Fails(List<EngineMessage> expected, String... variableReplacements) {
            matcher = allOf(
                    not(isValid()),
                    new WithMessages(expected),
                    hasVariableReplacements(variableReplacements));
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
