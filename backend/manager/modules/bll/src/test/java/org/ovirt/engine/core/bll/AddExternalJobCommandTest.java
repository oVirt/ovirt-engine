package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.common.action.AddExternalJobParameters;

public class AddExternalJobCommandTest {
    private AddExternalJobCommand<AddExternalJobParameters> command =
            new AddExternalJobCommand<>(new AddExternalJobParameters("test"), null);

    @Test
    public void validateDescriptionOkSucceeds() throws Exception {
        assertTrue(command.validate());
    }

    @Test
    public void validateEmptyDescriptionFails() throws Exception {
        command.getParameters().setDescription("");
        assertTrue(! command.validate());
    }

    @Test
    public void validateBlankDescriptionFails() throws Exception {
        command.getParameters().setDescription("      ");
        assertTrue(! command.validate());
    }
}
