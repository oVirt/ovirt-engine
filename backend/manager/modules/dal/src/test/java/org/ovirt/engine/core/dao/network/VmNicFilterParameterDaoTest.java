package org.ovirt.engine.core.dao.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;


public class VmNicFilterParameterDaoTest
        extends BaseGenericDaoTestCase<Guid, VmNicFilterParameter, VmNicFilterParameterDao> {

    @Override
    protected VmNicFilterParameter generateNewEntity() {
        VmNicFilterParameter parameter = new VmNicFilterParameter();
        parameter.setId(Guid.newGuid());
        parameter.setName("IP");
        parameter.setValue("192.168.122.2");
        parameter.setVmInterfaceId(FixturesTool.VM_NETWORK_INTERFACE);
        return  parameter;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setId(FixturesTool.VM_NETWORK_FILTER_PARAMETER);
        existingEntity.setName("IP");
        existingEntity.setValue("192.168.122.1");
        existingEntity.setVmInterfaceId(FixturesTool.VM_NETWORK_INTERFACE);
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.VM_NETWORK_FILTER_PARAMETER;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return FixturesTool.NUMBER_OF_VM_NETWORK_FILTER_PARAMETERS;
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
}
