package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;


public class VnicProfileDaoTest extends BaseGenericDaoTestCase<Guid, VnicProfile, VnicProfileDao> {
    @Override
    protected VnicProfile generateNewEntity() {
        VnicProfile vnicProfile = new VnicProfile();
        vnicProfile.setId(Guid.newGuid());
        vnicProfile.setName("new_profile");
        vnicProfile.setNetworkId(FixturesTool.NETWORK_ENGINE);
        vnicProfile.setNetworkQosId(FixturesTool.NETWORK_QOS);
        vnicProfile.setPortMirroring(false);
        vnicProfile.setPassthrough(false);
        vnicProfile.setNetworkFilterId(FixturesTool.VNIC_PROFILE_NETWORK_FILTER);
        vnicProfile.setMigratable(true);
        vnicProfile.setCustomProperties(Collections.emptyMap());
        vnicProfile.setFailoverVnicProfileId(FixturesTool.VM_NETWORK_INTERFACE_PASSTHROUGH_PROFILE);
        return vnicProfile;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setPortMirroring(true);
        existingEntity.setPassthrough(true);
        existingEntity.setMigratable(true);
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.VM_NETWORK_INTERFACE_PROFILE;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 5;
    }

    /**
     * Ensures that the network interface profile is returned.
     */
    @Test
    public void testGetWithPm() {
        VnicProfile result = dao.get(FixturesTool.VM_NETWORK_INTERFACE_PM_PROFILE);

        assertNotNull(result);
        assertEquals(FixturesTool.VM_NETWORK_INTERFACE_PM_PROFILE, result.getId());
        assertTrue(result.isPortMirroring());
    }

    /**
     * Ensures that the network interface profile is returned.
     */
    @Test
    public void testGetWithPassthrough() {
        VnicProfile result = dao
                .get(FixturesTool.VM_NETWORK_INTERFACE_PASSTHROUGH_PROFILE);

        assertNotNull(result);
        assertEquals(FixturesTool.VM_NETWORK_INTERFACE_PASSTHROUGH_PROFILE,
                result.getId());
        assertTrue(result.isPassthrough());
        assertFalse(result.isMigratable());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForNetworkEmpty() {
        List<VnicProfile> result = dao.getAllForNetwork(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that profiles are returned.
     */
    @Test
    public void testGetAllForNetworkFull() {
        List<VnicProfile> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Ensures that a single profile is returned.
     */
    @Test
    public void testGetAllForNetwork() {
        List<VnicProfile> result = dao.getAllForNetwork(FixturesTool.NETWORK_ENGINE);

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
