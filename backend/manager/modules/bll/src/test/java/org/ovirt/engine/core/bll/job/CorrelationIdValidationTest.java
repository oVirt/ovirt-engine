package org.ovirt.engine.core.bll.job;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.utils.CorrelationIdTracker;

public class CorrelationIdValidationTest {

    public void setUp() {
        CorrelationIdTracker.setCorrelationId(null);
    }

    @Test
    public void validCorrelationId() {
        ActionParametersBase parameters = new ActionParametersBase();
        parameters.setCorrelationId("VALID_CORRELATION_ID");
        ActionReturnValue result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNull(result, "Correlation id is valid");
    }

    @Test
    public void invalidCorrelationId() {
        ActionParametersBase parameters = new ActionParametersBase();
        parameters.setCorrelationId("INVALID_CORRELATION_@#$%@#");
        ActionReturnValue result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNotNull(result, "Correlation id is invalid");
        assertFalse(result.getSucceeded(), "Correlation id is invalid");
    }

    @Test
    public void correlationIdMaxSize() {
        ActionParametersBase parameters = new ActionParametersBase();
        parameters.setCorrelationId(StringUtils.leftPad("", BusinessEntitiesDefinitions.CORRELATION_ID_SIZE, "A"));
        ActionReturnValue result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNull(result, "Correlation id is size is at max permitted length");
    }

    @Test
    public void correlationIdTooLong() {
        ActionParametersBase parameters = new ActionParametersBase();
        parameters.setCorrelationId(StringUtils.leftPad("", BusinessEntitiesDefinitions.CORRELATION_ID_SIZE + 1, "A"));
        ActionReturnValue result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNotNull(result, "Correlation id exceeds max size");
        assertFalse(result.getSucceeded(), "Correlation id exceeds max size");
    }
}
