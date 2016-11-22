package org.ovirt.engine.api.restapi.resource;

import static org.junit.Assert.assertEquals;
import static org.ovirt.engine.api.restapi.resource.BackendGraphicsConsoleHelper.asConsoleId;
import static org.ovirt.engine.api.restapi.resource.BackendGraphicsConsoleHelper.asGraphicsType;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.businessentities.GraphicsType;

@RunWith(Theories.class)
public class BackendGraphicsConsoleHelperTest {

    @DataPoints
    public static GraphicsType[] TYPES = GraphicsType.values();

    @Theory
    public void asGraphicsTypeInverseOfAsConsoleId(GraphicsType type) {
        assertEquals(type, asGraphicsType(asConsoleId(type)));
    }

    private static final String VNC_ID = "766e63";
    private static final String SPICE_ID = "7370696365";

    @DataPoints
    public static String[] IDS = new String[] { VNC_ID, SPICE_ID };

    @Theory
    public void asConsoleIdInverseOfAsGraphicsType(String consoleId) {
        assertEquals(consoleId, asConsoleId(asGraphicsType(consoleId)));
    }
}
