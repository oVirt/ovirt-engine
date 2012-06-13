package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

public class EntitiesTest {

    private static final Guid[] GUIDs = {
            Guid.createGuidFromString("000000000000-0000-0000-0000-00000001"),
            Guid.createGuidFromString("000000000000-0000-0000-0000-00000002"),
            Guid.createGuidFromString("000000000000-0000-0000-0000-00000003"),
            Guid.createGuidFromString("000000000000-0000-0000-0000-00000004") };

    @Test
    public void businessEntitiesById() {
        List<VmDevice> list = new ArrayList<VmDevice>();

        VmDeviceId id1 = new VmDeviceId(GUIDs[0], GUIDs[1]);
        VmDeviceId id2 = new VmDeviceId(GUIDs[2], GUIDs[3]);

        VmDevice d1 = new VmDevice();
        d1.setId(id1);
        VmDevice d2 = new VmDevice();
        d2.setId(id2);

        list.add(d1);
        list.add(d2);

        Map<VmDeviceId, VmDevice> businessEntitiesById = Entities.businessEntitiesById(list);

        Assert.assertTrue(businessEntitiesById.containsKey(id1));
        Assert.assertTrue(businessEntitiesById.containsKey(id2));

        Assert.assertFalse(businessEntitiesById.containsKey(new VmDeviceId(GUIDs[0], GUIDs[3])));
    }

    @Test
    public void objectNames() {
        List<Network> list = new ArrayList<Network>();
        Network n1 = new Network();
        n1.setname("network1");
        Network n2 = new Network();
        n2.setname("network2");
        list.add(n1);
        list.add(n2);
        Set<String> names = Entities.objectNames(list);
        Assert.assertTrue(names.size() == 2);
        Assert.assertTrue(names.contains("network1"));
        Assert.assertTrue(names.contains("network2"));
        Assert.assertFalse(names.contains("network3"));
        Assert.assertTrue(Entities.objectNames(null).equals(Collections.emptySet()));
        Assert.assertTrue(Entities.objectNames(new ArrayList<Network>()).equals(Collections.emptySet()));
    }

}
