package org.ovirt.engine.core.common.businessentities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Guid;

public class EntitiesTest {

    private static final Guid[] GUIDs = {
            new Guid("000000000000-0000-0000-0000-00000001"),
            new Guid("000000000000-0000-0000-0000-00000002"),
            new Guid("000000000000-0000-0000-0000-00000003"),
            new Guid("000000000000-0000-0000-0000-00000004") };

    @Test
    public void businessEntitiesById() {
        List<VmDevice> list = new ArrayList<>();

        VmDeviceId id1 = new VmDeviceId(GUIDs[0], GUIDs[1]);
        VmDeviceId id2 = new VmDeviceId(GUIDs[2], GUIDs[3]);

        VmDevice d1 = new VmDevice();
        d1.setId(id1);
        VmDevice d2 = new VmDevice();
        d2.setId(id2);

        list.add(d1);
        list.add(d2);

        Map<VmDeviceId, VmDevice> businessEntitiesById = Entities.businessEntitiesById(list);

        assertTrue(businessEntitiesById.containsKey(id1));
        assertTrue(businessEntitiesById.containsKey(id2));

        assertFalse(businessEntitiesById.containsKey(new VmDeviceId(GUIDs[0], GUIDs[3])));
    }
}
