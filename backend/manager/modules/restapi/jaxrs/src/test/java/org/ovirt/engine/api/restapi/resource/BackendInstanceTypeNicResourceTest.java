/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendInstanceTypeNicResourceTest
        extends AbstractBackendSubResourceTest<Nic, VmNetworkInterface, BackendInstanceTypeNicResource> {

    private static final Guid INSTANCE_TYPE_ID = GUIDS[1];
    private static final Guid NIC_ID = GUIDS[0];

    public BackendInstanceTypeNicResourceTest() {
        super(new BackendInstanceTypeNicResource(NIC_ID.toString(), INSTANCE_TYPE_ID));
    }

    @Test
    public void testRemove() {
        setUpGetNicsExpectations();
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveVmTemplateInterface,
                RemoveVmTemplateInterfaceParameters.class,
                new String[] { "VmTemplateId", "InterfaceId" },
                new Object[] { INSTANCE_TYPE_ID, NIC_ID },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetNicsExpectations();
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveVmTemplateInterface,
                RemoveVmTemplateInterfaceParameters.class,
                new String[] { "VmTemplateId", "InterfaceId" },
                new Object[] { INSTANCE_TYPE_ID, NIC_ID },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    private void setUpGetNicsExpectations() {
        setUpEntityQueryExpectations(
            QueryType.GetTemplateInterfacesByTemplateId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { INSTANCE_TYPE_ID },
            setUpNicsExpectations()
        );
    }

    private List<VmNetworkInterface> setUpNicsExpectations() {
        List<VmNetworkInterface> nics = new ArrayList<>();
        nics.add(setUpNicExpectations());
        return nics;
    }

    private VmNetworkInterface setUpNicExpectations() {
        VmNetworkInterface nic = mock(VmNetworkInterface.class);
        when(nic.getId()).thenReturn(NIC_ID);
        when(nic.getType()).thenReturn(0);
        return nic;
    }
}
