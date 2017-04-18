
package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.core.common.action.AttachUserToVmFromPoolAndRunParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendVmPoolResourceTest
        extends AbstractBackendSubResourceTest<VmPool, org.ovirt.engine.core.common.businessentities.VmPool, BackendVmPoolResource> {

    public BackendVmPoolResourceTest() {
        super(new BackendVmPoolResource(GUIDS[0].toString(), new BackendVmPoolsResource()));
    }

    @Override
    protected void init() {
        super.init();
        resource.getParent().mappingLocator = resource.mappingLocator;
        resource.getParent().httpHeaders = httpHeaders;
    }

    @Test
    public void testBadGuid() throws Exception {
        try {
            new BackendVmPoolResource("foo", new BackendVmPoolsResource());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testAllocateVm() throws Exception {
        setUpGetVmExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.AttachUserToVmFromPoolAndRun,
                                           AttachUserToVmFromPoolAndRunParameters.class,
                                           new String[] { "VmPoolId" },
                                           new Object[] { GUIDS[0] },
                                           GUIDS[0]));

        verifyTestAllocateVmActionResponse(resource.allocateVm(new Action()));
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmPool,
                VmPoolParametersBase.class,
                new String[] { "VmPoolId" },
                new Object[] { GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    private void setUpGetVmExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       getVmEntity());
        }
    }

    private VM getVmEntity() {
        return getVmEntity(0);
    }

    protected VM getVmEntity(int index) {
        return setUpVmEntityExpectations(
                mock(VM.class),
                index);
    }

    private VM setUpVmEntityExpectations(VM entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);

        return entity;
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        setUpGetEntityExpectations(times, notFound, getEntity(0));
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound, org.ovirt.engine.core.common.businessentities.VmPool entity) throws Exception {

        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVmPoolById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              Object taskReturn) {
        return setUpActionExpectations(task, clz, names, values, true, true, taskReturn, null, true);
    }

    private void verifyTestAllocateVmActionResponse(Response r) throws Exception {
        assertNotNull(r.getEntity());
        assertNotNull(((org.ovirt.engine.api.model.Action)r.getEntity()).getVm());
        assertNotNull(((org.ovirt.engine.api.model.Action)r.getEntity()).getVm().getId());
        assertEquals(((org.ovirt.engine.api.model.Action)r.getEntity()).getVm().getId(), GUIDS[0].toString());

        verifyActionResponse(r, "vmpools/" + GUIDS[0], false);
    }

    @Override
    protected void verifyModel(VmPool model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model);
    }

    static void verifyModelSpecific(VmPool model) {
        assertNotNull(model.getCluster());
        assertNotNull(model.getCluster().getId());
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VmPool getEntity(int index) {
        return setUpEntityExpectations(
                mock(org.ovirt.engine.core.common.businessentities.VmPool.class),
                index);
    }

    private org.ovirt.engine.core.common.businessentities.VmPool setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.VmPool entity,
            int index) {
        when(entity.getVmPoolId()).thenReturn(GUIDS[index]);
        when(entity.getClusterId()).thenReturn(GUIDS[2]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getVmPoolType()).thenReturn(VmPoolType.AUTOMATIC);
        when(entity.getVmPoolDescription()).thenReturn(DESCRIPTIONS[index]);

        return entity;
    }
}
