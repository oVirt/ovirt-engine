package org.ovirt.engine.ui.uicommonweb.models.vms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel.GraphicsTypes;


public class GraphicsTypesTest {

    @Test
    public void testGetBackingGraphicsType() {
        assertEquals(makeSet(), GraphicsTypes.NONE.getBackingGraphicsTypes());
        assertEquals(makeSet(GraphicsType.SPICE), GraphicsTypes.SPICE.getBackingGraphicsTypes());
        assertEquals(makeSet(GraphicsType.VNC), GraphicsTypes.VNC.getBackingGraphicsTypes());
        assertEquals(makeSet(GraphicsType.SPICE, GraphicsType.VNC), GraphicsTypes.SPICE_AND_VNC.getBackingGraphicsTypes());
    }

    @Test
    public void testFromGraphicsType() {
        assertEquals(GraphicsTypes.SPICE, GraphicsTypes.fromGraphicsType(GraphicsType.SPICE));
        assertEquals(GraphicsTypes.VNC, GraphicsTypes.fromGraphicsType(GraphicsType.VNC));
    }

    @Test
    public void testFromGraphicsTypes() {
        assertEquals(GraphicsTypes.NONE, GraphicsTypes.fromGraphicsTypes(null));
        assertEquals(GraphicsTypes.SPICE, GraphicsTypes.fromGraphicsTypes(makeSet(GraphicsType.SPICE)));
        assertEquals(GraphicsTypes.VNC, GraphicsTypes.fromGraphicsTypes(makeSet(GraphicsType.VNC)));
    }

    private <T> Set<T> makeSet(T ... elems) {
        return new HashSet<>(Arrays.asList(elems));
    }

}
