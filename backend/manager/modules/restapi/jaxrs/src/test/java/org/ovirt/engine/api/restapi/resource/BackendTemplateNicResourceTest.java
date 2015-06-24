package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.expect;

public class BackendTemplateNicResourceTest
        extends AbstractBackendSubResourceTest<NIC, VmNetworkInterface, BackendTemplateNicResource> {

    private static final Guid TEMPLATE_ID = GUIDS[0];
    private static final Guid NIC_ID = GUIDS[1];

    public BackendTemplateNicResourceTest() {
        super((BackendTemplateNicResource) getCollection().getDeviceSubResource(NIC_ID.toString()));
    }

    private static BackendTemplateNicsResource getCollection() {
        return new BackendTemplateNicsResource(TEMPLATE_ID);
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetNicsExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveVmTemplateInterface,
                RemoveVmTemplateInterfaceParameters.class,
                new String[] { "VmTemplateId", "InterfaceId" },
                new Object[] { TEMPLATE_ID, NIC_ID },
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

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetNicsExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveVmTemplateInterface,
                RemoveVmTemplateInterfaceParameters.class,
                new String[] { "VmTemplateId", "InterfaceId" },
                new Object[] { TEMPLATE_ID, NIC_ID },
                canDo,
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
            new Object[] { TEMPLATE_ID },
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
