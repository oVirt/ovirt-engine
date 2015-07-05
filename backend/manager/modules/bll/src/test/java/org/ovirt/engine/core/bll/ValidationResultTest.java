package org.ovirt.engine.core.bll;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import org.junit.Test;
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
    public void invalidWhenConditionOccurs() throws Exception {
        assertThat(ValidationResult.failWith(ERROR).when(true), failsWith(ERROR));
    }

    @Test
    public void validWhenConditionDoesntOccur() throws Exception {
        assertThat(ValidationResult.failWith(ERROR).when(false), isValid());
    }

    @Test
    public void validUnlessConditionOccurs() throws Exception {
        assertThat(ValidationResult.failWith(ERROR).unless(true), isValid());
    }

    @Test
    public void invalidUnlessConditionDoesntOccur() throws Exception {
        assertThat(ValidationResult.failWith(ERROR).unless(false), failsWith(ERROR));
    }

    @Test
    public void invalidWhenConditionDoesntOccurWithSingleReplacement() throws Exception {
        assertThat(ValidationResult.failWith(ERROR, REPLACEMENT).when(true),
                both(failsWith(ERROR)).and(replacements(hasItem(REPLACEMENT))));
    }

    @Test
    public void invalidWhenConditionDoesntOccurWithMultipleReplacements() throws Exception {
        assertThat(ValidationResult.failWith(ERROR, REPLACEMENT, OTHER_REPLACEMENT).when(true),
                both(failsWith(ERROR)).and(replacements(hasItem(REPLACEMENT)))
                        .and(replacements(hasItem(OTHER_REPLACEMENT))));
    }

    @Test
    public void invalidWhenConditionOccursWithVariableReplacements() throws Exception {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).when(true),
                failsWith(ERROR, variableReplacements));
    }

    @Test
    public void validWhenConditionDoesntOccurWithVariableReplacements() throws Exception {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).when(false), isValid());
    }

    @Test
    public void validUnlessConditionOccursWithVariableReplacements() throws Exception {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).unless(true), isValid());
    }

    @Test
    public void invalidUnlessConditionDoesntOccurWithVariableReplacements() throws Exception {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).unless(false),
                failsWith(ERROR, variableReplacements));
    }

    @Test
    public void invalidUnlessConditionDoesntOccurWithVariableReplacements2() throws Exception {
        assertThat(ValidationResult.failWith(ERROR, variableReplacements).unless(false),
                failsWith(ERROR, variableReplacements));

    }
}
