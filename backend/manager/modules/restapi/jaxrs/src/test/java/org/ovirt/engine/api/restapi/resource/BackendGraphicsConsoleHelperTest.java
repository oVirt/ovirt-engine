package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.ovirt.engine.api.restapi.resource.BackendGraphicsConsoleHelper.asConsoleId;
import static org.ovirt.engine.api.restapi.resource.BackendGraphicsConsoleHelper.asGraphicsType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.GraphicsType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendGraphicsConsoleHelperTest {
    @ParameterizedTest
    @EnumSource(value = GraphicsType.class)
    public void asGraphicsTypeInverseOfAsConsoleId(GraphicsType type) {
        assertEquals(type, asGraphicsType(asConsoleId(type)));
    }

    private static final String VNC_ID = "766e63";
    private static final String SPICE_ID = "7370696365";

    @ParameterizedTest
    @ValueSource(strings = {VNC_ID, SPICE_ID})
    public void asConsoleIdInverseOfAsGraphicsType(String consoleId) {
        assertEquals(consoleId, asConsoleId(asGraphicsType(consoleId)));
    }
}
