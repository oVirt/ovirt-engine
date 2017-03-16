package org.mockito.configuration;

import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.ValidationResult;

/**
 * Overrides Mockito's configuration for BLL's tests with a default answer that stubbs validation results with
 * {@link ValidationResult#VALID} by default.
 */
public class MockitoConfiguration extends DefaultMockitoConfiguration {
    @Override
    public Answer<Object> getDefaultAnswer() {
        Answer<Object> defaultAnswer = super.getDefaultAnswer();
        return invocation -> {
            if (invocation.getMethod().getReturnType().equals(ValidationResult.class)) {
                return ValidationResult.VALID;
            }
            return defaultAnswer.answer(invocation);
        };
    }
}
