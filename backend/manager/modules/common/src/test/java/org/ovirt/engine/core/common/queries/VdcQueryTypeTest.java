package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

/** A test case for the {@link VdcQueryType} class. */
public class VdcQueryTypeTest {

    @Test
    public void testForValue() {
        assertEquals("wrong value",
                VdcQueryType.forValue(VdcQueryType.GetAgentFenceOptions2.getValue()),
                VdcQueryType.GetAgentFenceOptions2);
    }

    @SuppressWarnings("cast")
    @Test
    public void testSerializable() {
        // Prove that it's still serializable even though explicit definition has been removed.
        assertTrue(VdcQueryType.CanFenceVds instanceof Serializable);
    }

}
