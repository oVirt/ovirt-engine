package org.ovirt.engine.core.bll.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
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
        assertNull("Correlation id is valid", result);
    }

    @Test
    public void invalidCorrelationId() {
        ActionParametersBase parameters = new ActionParametersBase();
        parameters.setCorrelationId("INVALID_CORRELATION_@#$%@#");
        ActionReturnValue result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNotNull("Correlation id is invalid", result);
        assertFalse("Correlation id is invalid", result.getSucceeded());
    }

    @Test
    public void correlationIdMaxSize() {
        ActionParametersBase parameters = new ActionParametersBase();
        parameters.setCorrelationId(StringUtils.leftPad("", BusinessEntitiesDefinitions.CORRELATION_ID_SIZE, "A"));
        ActionReturnValue result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNull("Correlation id is size is at max permitted length", result);
    }

    @Test
    public void correlationIdTooLong() {
        ActionParametersBase parameters = new ActionParametersBase();
        parameters.setCorrelationId(StringUtils.leftPad("", BusinessEntitiesDefinitions.CORRELATION_ID_SIZE + 1, "A"));
        ActionReturnValue result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNotNull("Correlation id exceeds max size", result);
        assertFalse("Correlation id exceeds max size", result.getSucceeded());
    }
}
