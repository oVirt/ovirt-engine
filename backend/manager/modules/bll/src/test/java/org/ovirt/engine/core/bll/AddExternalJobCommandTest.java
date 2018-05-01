package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.common.action.AddExternalJobParameters;

public class AddExternalJobCommandTest {
    private AddExternalJobCommand<AddExternalJobParameters> command =
            new AddExternalJobCommand<>(new AddExternalJobParameters("test"), null);

    @Test
    public void validateDescriptionOkSucceeds() {
        assertTrue(command.validate());
    }

    @Test
    public void validateEmptyDescriptionFails() {
        command.getParameters().setDescription("");
        assertTrue(! command.validate());
    }

    @Test
    public void validateBlankDescriptionFails() {
        command.getParameters().setDescription("      ");
        assertTrue(! command.validate());
    }
}
