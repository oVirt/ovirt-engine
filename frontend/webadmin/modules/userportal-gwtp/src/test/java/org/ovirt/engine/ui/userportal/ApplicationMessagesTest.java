package org.ovirt.engine.ui.userportal;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.ui.uicompat.GwtMessagesValidator;

public class ApplicationMessagesTest {

    @Test
    public void doTest() throws URISyntaxException, IOException {
        List<String> errors = GwtMessagesValidator.validateClass(ApplicationMessages.class);
        assertTrue(GwtMessagesValidator.format(errors), errors.isEmpty());
    }

}
