package org.ovirt.engine.ui.uicommonweb.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class AlternativeValidationTest {
    @Test
    public void testAlteringPatternsWithMultipleFailingPatterns() {
        List<IValidation> validations = Arrays.asList(new RegexValidation("^$", ""), new RegexValidation("^b$", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        doTest(validations, false);
    }

    @Test
    public void testAlteringPatternsWithMultiplePatternsSecondValid() {
        List<IValidation> validations = Arrays.asList(new RegexValidation("^$", ""), new RegexValidation("^a.*$", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        doTest(validations, true);
    }

    @Test
    public void testAlteringPatternsWithMultiplePatternsFirstValid() {
        List<IValidation> validations = Arrays.asList(new RegexValidation("^a.*$", ""), new RegexValidation("^$", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        doTest(validations, true);
    }

    @Test
    public void testAlteringValidationWithNoValidation() {
        doTest(Collections.emptyList(), true);
    }

    private void doTest(List<IValidation> validations, boolean expectedToSucceed) {
        String reason = "reason"; //$NON-NLS-1$

        ValidationResult validationResult = new AlternativeValidation(reason, validations).validate("abc"); //$NON-NLS-1$
        assertThat(validationResult.getSuccess(), is(expectedToSucceed));
        if (expectedToSucceed) {
            assertThat(validationResult.getReasons(), is(emptyCollectionOf(String.class)));
        } else {
            assertThat(validationResult.getReasons(), is(Matchers.containsInAnyOrder(reason)));
        }
    }
}
