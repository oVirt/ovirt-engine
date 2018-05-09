
package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachUserToVmFromPoolAndRunParameters;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(
                WebApplicationException.class, () -> new BackendVmPoolResource("foo", new BackendVmPoolsResource())));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testAllocateVm() {
        setUpGetVmExpectations(1);
        setUriInfo(setUpActionExpectations(ActionType.AttachUserToVmFromPoolAndRun,
                                           AttachUserToVmFromPoolAndRunParameters.class,
                                           new String[] { "VmPoolId" },
                                           new Object[] { GUIDS[0] },
                                           GUIDS[0]));

        verifyTestAllocateVmActionResponse(resource.allocateVm(new Action()));
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(ActionType.RemoveVmPool,
                VmPoolParametersBase.class,
                new String[] { "VmPoolId" },
                new Object[] { GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    private void setUpGetVmExpectations(int times) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetVmByVmId,
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

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        setUpGetEntityExpectations(times, notFound, getEntity(0));
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound, org.ovirt.engine.core.common.businessentities.VmPool entity) {

        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetVmPoolById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    protected UriInfo setUpActionExpectations(ActionType task,
                                              Class<? extends ActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              Object taskReturn) {
        return setUpActionExpectations(task, clz, names, values, true, true, taskReturn, null, true);
    }

    private void verifyTestAllocateVmActionResponse(Response r) {
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
