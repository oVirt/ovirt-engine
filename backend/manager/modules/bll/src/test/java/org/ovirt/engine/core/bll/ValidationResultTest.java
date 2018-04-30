package org.ovirt.engine.core.bll;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class ValidationResultTest {

    /**
     * Some message to use with the tests, we don't care what it actually is.
     */
    private static final EngineMessage ERROR = EngineMessage.values()[0];
    private static final String REPLACEMENT = "replacement";
    private static final String OTHER_REPLACEMENT = "other_replacement";
    private final String[] variableReplacements = new String[] { "a", "b" };

    @Test
    public void invalidWhenConditionOccurs() {
        assertThat(ValidationResult.failWith(ERROR).when(true), failsWith(ERROR));
    }

    @Test
    public void validWhenConditionDoesntOccur() {
        assertThat(ValidationResult.failWith(ERROR).when(false), isValid());
    }

    @Test
    public void validUnlessConditionOccurs() {
        assertThat(ValidationResult.failWith(ERROR).unless(true), isValid());
    }

    @Test
    public void invalidUnlessConditionDoesntOccur() {
        assertThat(ValidationResult.failWith(ERROR).unless(false), failsWith(ERROR));
    }

    @Test
    public void invalidWhenConditionDoesntOccurWithSingleReplacement() {
        assertThat(ValidationResult.failWith(ERROR, REPLACEMENT).when(true),
                both(failsWith(ERROR)).and(replacements(hasItem(REPLACEMENT))));
    }

    @Test
    public void invalidWhenConditionDoesntOccurWithMultipleReplacements() {
        assertThat(ValidationResult.failWith(ERROR, REPLACEMENT, OTHER_REPLACEMENT).when(true),
                both(failsWith(ERROR)).and(replacements(hasItem(REPLACEMENT)))
                        .and(replacements(hasItem(OTHER_REPLACEMENT))));
    }

    @Test
    public void invalidWhenConditionOccursWithVariableReplacements() {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).when(true),
                failsWith(ERROR, variableReplacements));
    }

    @Test
    public void validWhenConditionDoesntOccurWithVariableReplacements() {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).when(false), isValid());
    }

    @Test
    public void validUnlessConditionOccursWithVariableReplacements() {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).unless(true), isValid());
    }

    @Test
    public void invalidUnlessConditionDoesntOccurWithVariableReplacements() {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).unless(false),
                failsWith(ERROR, variableReplacements));
    }

    @Test
    public void invalidUnlessConditionDoesntOccurWithVariableReplacements2() {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).unless(false),
                failsWith(ERROR, variableReplacements));
    }
}
