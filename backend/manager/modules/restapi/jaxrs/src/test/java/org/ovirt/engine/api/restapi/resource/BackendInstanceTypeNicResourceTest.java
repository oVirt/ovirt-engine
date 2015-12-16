/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeNicResourceTest
        extends AbstractBackendSubResourceTest<Nic, VmNetworkInterface, BackendInstanceTypeNicResource> {

    private static final Guid INSTANCE_TYPE_ID = GUIDS[1];
    private static final Guid NIC_ID = GUIDS[0];

    public BackendInstanceTypeNicResourceTest() {
        super(new BackendInstanceTypeNicResource(NIC_ID.toString(), INSTANCE_TYPE_ID));
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetNicsExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveVmTemplateInterface,
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
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpGetNicsExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveVmTemplateInterface,
                RemoveVmTemplateInterfaceParameters.class,
                new String[] { "VmTemplateId", "InterfaceId" },
                new Object[] { INSTANCE_TYPE_ID, NIC_ID },
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    private void setUpGetNicsExpectations() {
        setUpEntityQueryExpectations(
            VdcQueryType.GetTemplateInterfacesByTemplateId,
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
        VmNetworkInterface nic = control.createMock(VmNetworkInterface.class);
        expect(nic.getId()).andReturn(NIC_ID).anyTimes();
        expect(nic.getType()).andReturn(0).anyTimes();
        return nic;
    }
}
