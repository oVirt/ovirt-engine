package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.provider.HostProviderBindingDao;

public class HostProviderBindingDaoImplTest extends BaseDaoTestCase<HostProviderBindingDao> {
    @Inject
    private VdsStaticDao vdsStaticDao;

    /**
     * Ensures proper value is returned
     */
    @Test
    public void testGet() {
        String result = dao.get(
            FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID,
            FixturesTool.PROVIDER_BINDING_HOST_ID_PLUGIN_TYPE);

        assertEquals(FixturesTool.PROVIDER_BINDING_HOST_PLUGIN_ID, result);
    }

    /**
     * Ensures no value is returned for a non existing plugin type
     */
    @Test
    public void testGetNonexistingPluginType() {
        String result = dao.get(
            FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID, "anything");
        assertNull(result);
    }

    /**
     * Ensures no value is returned for a non existing vds
     */
    @Test
    public void testGetNonexistingVds() {
        String result = dao.get(
            Guid.newGuid(), FixturesTool.PROVIDER_BINDING_HOST_ID_PLUGIN_TYPE);
        assertNull(result);
    }

    /**
     * Ensures the value is updated properly
     */
    @Test
    public void testUpdate() {
        Guid vdsId = Guid.newGuid();
        String plugin1 = "OVS";
        String hostId1 = "bee5-1590d-bee5-15g00d";
        String plugin2 = "other-provider";
        String hostId2 = "other value";
        Map<String, Object> values = new HashMap();
        values.put(plugin1, hostId1);
        values.put(plugin2, hostId2);

        dao.update(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID, values);

        String result = dao.get(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID, plugin1);
        assertEquals(hostId1, result);
        result = dao.get(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID, plugin2);
        assertEquals(hostId2, result);
    }

    /**
     * Ensures the value is removed when not present in update
     */
    @Test
    public void testValueIsRemovedIfNotPresentInUpdate() {
        Guid vdsId = Guid.newGuid();
        String plugin = "OVS";
        String hostId = "bee5-1590d-bee5-15g00d";
        Map<String, Object> values = new HashMap();
        values.put(plugin, hostId);

        dao.update(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID, values);
        String result = dao.get(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID, plugin);
        assertEquals(hostId, result);

        values = new HashMap();
        values.put("OTHER", hostId);

        dao.update(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID, values);
        result = dao.get(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID, plugin);
        assertNull(result);
    }

    /**
     * Ensures the value is removed when host is removed
     */
    @Test
    public void testRemoveHost() {
        String plugin = "OVS";
        String hostId = "bee5-1590d-bee5-15g00d";
        Map<String, Object> values = new HashMap();
        values.put(plugin, hostId);
        dao.update(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID2, values);

        String result = dao.get(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID2, plugin);
        assertEquals(hostId, result);

        vdsStaticDao.remove(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID2);

        String result2 = dao.get(FixturesTool.PROVIDER_BINDING_HOST_ID_HOST_ID2, plugin);
        assertNull(result2);
    }
}
