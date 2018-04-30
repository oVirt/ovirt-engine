package org.ovirt.engine.ui.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.ui.uicompat.GwtMessagesValidator;

public class CommonApplicationMessagesTest {

    @Test
    public void doTest() throws URISyntaxException, IOException {
        List<String> errors = GwtMessagesValidator.validateClass(CommonApplicationMessages.class);
        assertTrue(errors.isEmpty(), GwtMessagesValidator.format(errors));
    }

}
