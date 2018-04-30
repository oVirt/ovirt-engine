package org.ovirt.engine.ui.uicompat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;

public class UIMessagesTest {
    @Test
    public void doTest() throws URISyntaxException, IOException {
        List<String> errors = GwtMessagesValidator.validateClass(UIMessages.class);
        assertTrue(errors.isEmpty(), GwtMessagesValidator.format(errors));
    }
}
