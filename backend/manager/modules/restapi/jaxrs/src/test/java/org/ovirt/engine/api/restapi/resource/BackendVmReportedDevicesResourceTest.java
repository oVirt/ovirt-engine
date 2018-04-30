package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVmReportedDevicesResourceTest extends AbstractBackendCollectionResourceTest<ReportedDevice, VmGuestAgentInterface, BackendVmReportedDevicesResource> {

    protected static final Guid PARENT_ID = GUIDS[1];
    protected static final String[] ADDRESSES = { "10.11.12.13", "13.12.11.10", "10.01.10.01" };

    public BackendVmReportedDevicesResourceTest() {
        super(new BackendVmReportedDevicesResource(PARENT_ID), null, "");

    }

    @Override
    protected List<ReportedDevice> getCollection() {
        return collection.list().getReportedDevices();
    }

    @Override
    protected void verifyModel(ReportedDevice model, int index) {
        assertEquals(NAMES[index], model.getName());
        assertEquals(PARENT_ID.toString(), model.getVm().getId());
        verifyIps(model);
        verifyLinks(model);
    }

    private void verifyIps(ReportedDevice device) {
        List<Ip> ips = device.getIps().getIps();
        assertEquals(ADDRESSES.length, ips.size());
        for (int i = 0; i < ADDRESSES.length; i++) {
            assertEquals(ADDRESSES[i], ips.get(i).getAddress());
        }
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(QueryType.GetVmGuestAgentInterfacesByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { PARENT_ID },
                getEntities(),
                failure);
    }

    @Override
    protected VmGuestAgentInterface getEntity(int index) {
        VmGuestAgentInterface entity = new VmGuestAgentInterface();
        entity.setInterfaceName(NAMES[index]);
        entity.setIpv4Addresses(Arrays.asList(ADDRESSES));
        entity.setVmId(PARENT_ID);
        return entity;
    }

    protected List<VmGuestAgentInterface> getEntities() {
        List<VmGuestAgentInterface> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }
}
