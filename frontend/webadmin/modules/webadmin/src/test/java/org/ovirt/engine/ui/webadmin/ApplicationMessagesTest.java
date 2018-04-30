package org.ovirt.engine.ui.webadmin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.ui.uicompat.GwtMessagesValidator;

public class ApplicationMessagesTest {

    @Test
    public void doTest() throws URISyntaxException, IOException {
        List<String> errors = GwtMessagesValidator.validateClass(ApplicationMessages.class);
        assertTrue(errors.isEmpty(), GwtMessagesValidator.format(errors));
    }

}
