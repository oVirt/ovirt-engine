package org.ovirt.engine.core.dao.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;


public class VmNicFilterParameterDaoTest extends BaseDaoTestCase {

    @Inject
    private VmNicFilterParameterDao dao;

    /**
     * Ensures null is returned.
     */
    @Test
    public void testGetWithNonExistingId() {
        VmNicFilterParameter result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that the network filter parameter is returned.
     */
    @Test
    public void testGet() {
        VmNicFilterParameter result = dao.get(FixturesTool.VM_NETWORK_FILTER_PARAMETER);

        assertNotNull(result);
        assertEquals(FixturesTool.VM_NETWORK_FILTER_PARAMETER, result.getId());
    }

    /**
     * Ensures that the expected number of network filter parameters is returned.
     */
    @Test
    public void testGetAll() {
        List<VmNicFilterParameter> parameters = dao.getAll();
        assertNotNull(parameters);
        assertEquals(FixturesTool.NUMBER_OF_VM_NETWORK_FILTER_PARAMETERS, parameters.size());
    }

    /**
     * Ensures that the returned network filter parameters are associated to the network interface.
     */
    @Test
    public void testGetAllForVmNic() {
        List<VmNicFilterParameter> result = dao.getAllForVmNic(FixturesTool.VM_NETWORK_INTERFACE);
        assertNotNull(result);
        assertEquals(FixturesTool.NUMBER_OF_VM_NETWORK_FILTER_PARAMETERS_OF_VM_NETWORK_INTERFACE, result.size());
        for (VmNicFilterParameter parameter : result) {
            assertEquals(FixturesTool.VM_NETWORK_INTERFACE, parameter.getVmInterfaceId());
        }
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllInterfacesForVmWithInvalidNic() {
        List<VmNicFilterParameter> result = dao.getAllForVmNic(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that after an update, the network filter parameter is indeed persisted with new values.
     */
    @Test
    public void testUpdate() {
        VmNicFilterParameter updatedParameter = new VmNicFilterParameter();
        updatedParameter.setId(FixturesTool.VM_NETWORK_FILTER_PARAMETER);
        updatedParameter.setName("IP");
        updatedParameter.setValue("192.168.122.1");
        updatedParameter.setVmInterfaceId(FixturesTool.VM_NETWORK_INTERFACE);

        assertNotEquals(updatedParameter, dao.get(FixturesTool.VM_NETWORK_FILTER_PARAMETER));
        dao.update(updatedParameter);
        assertEquals(updatedParameter, dao.get(FixturesTool.VM_NETWORK_FILTER_PARAMETER));
    }

    /**
     * Ensures that a pre-existing network filter parameter is removed.
     */
    @Test
    public void testRemove() {
        assertNotNull(dao.get(FixturesTool.VM_NETWORK_FILTER_PARAMETER));
        dao.remove(FixturesTool.VM_NETWORK_FILTER_PARAMETER);
        assertNull(dao.get(FixturesTool.VM_NETWORK_FILTER_PARAMETER));
    }

    /**
     * Ensures that a newly-created network filter parameter is properly persisted.
     */
    @Test
    public void testSave() {
        VmNicFilterParameter parameter = new VmNicFilterParameter();
        parameter.setId(Guid.newGuid());
        parameter.setName("IP");
        parameter.setValue("192.168.122.2");
        parameter.setVmInterfaceId(FixturesTool.VM_NETWORK_INTERFACE);

        assertNull(dao.get(parameter.getId()));
        dao.save(parameter);
        assertEquals(parameter, dao.get(parameter.getId()));
    }
}
