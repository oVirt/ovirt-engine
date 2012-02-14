package org.ovirt.engine.core.bll.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;

public class CorrelationIdValidationTest {

    @Test
    public void validCorrelationId() {
        VdcActionParametersBase parameters = new VdcActionParametersBase();
        parameters.setCorrelationId("VALID_CORRELATION_ID");
        VdcReturnValueBase result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNull("Correlation id is valid", result);
    }

    @Test
    public void invalidCorrelationId() {
        VdcActionParametersBase parameters = new VdcActionParametersBase();
        parameters.setCorrelationId("INVALID_CORRELATION_@#$%@#");
        VdcReturnValueBase result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNotNull("Correlation id is invalid", result);
        assertFalse("Correlation id is invalid", result.getSucceeded());
    }

    @Test
    public void correlationIdMaxSize() {
        VdcActionParametersBase parameters = new VdcActionParametersBase();
        parameters.setCorrelationId(StringUtils.leftPad("", BusinessEntitiesDefinitions.CORRELATION_ID_SIZE, "A"));
        VdcReturnValueBase result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNull("Correlation id is size is at max permitted length", result);
    }

    @Test
    public void correlationIdTooLong() {
        VdcActionParametersBase parameters = new VdcActionParametersBase();
        parameters.setCorrelationId(StringUtils.leftPad("", BusinessEntitiesDefinitions.CORRELATION_ID_SIZE + 1, "A"));
        VdcReturnValueBase result = ExecutionHandler.evaluateCorrelationId(parameters);
        assertNotNull("Correlation id exceeds max size", result);
        assertFalse("Correlation id exceeds max size", result.getSucceeded());
    }
}
