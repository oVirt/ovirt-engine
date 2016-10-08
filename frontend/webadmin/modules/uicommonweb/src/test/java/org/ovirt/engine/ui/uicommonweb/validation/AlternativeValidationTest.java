package org.ovirt.engine.ui.uicommonweb.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

public class AlternativeValidationTest {
    @Test
    public void testAlteringPatternsWithMultipleFailingPatterns() throws Exception {
        List<IValidation> validations = Arrays.<IValidation>asList(new RegexValidation("^$", ""), new RegexValidation("^b$", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        doTest(validations, false);
    }

    @Test
    public void testAlteringPatternsWithMultiplePatternsSecondValid() throws Exception {
        List<IValidation> validations = Arrays.<IValidation>asList(new RegexValidation("^$", ""), new RegexValidation("^a.*$", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        doTest(validations, true);
    }

    @Test
    public void testAlteringPatternsWithMultiplePatternsFirstValid() throws Exception {
        List<IValidation> validations = Arrays.<IValidation>asList(new RegexValidation("^a.*$", ""), new RegexValidation("^$", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        doTest(validations, true);
    }

    @Test
    public void testAlteringValidationWithNoValidation() throws Exception {
        doTest(Collections.<IValidation> emptyList(), true);
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
