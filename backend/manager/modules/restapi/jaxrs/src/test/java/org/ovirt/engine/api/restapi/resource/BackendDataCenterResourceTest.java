package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendDataCentersResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendDataCentersResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendDataCentersResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Version;

public class BackendDataCenterResourceTest
        extends AbstractBackendSubResourceTest<DataCenter, StoragePool, BackendDataCenterResource> {

    public BackendDataCenterResourceTest() {
        super(new BackendDataCenterResource(GUIDS[0].toString(), new BackendDataCentersResource()));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendDataCenterResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
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
        setUpVersionExpectations();
        setUpGetEntityExpectations(1);
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpVersionExpectations();
        setUpGetEntityExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateStoragePool,
                                           StoragePoolManagementParameter.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateStoragePool,
                                           StoragePoolManagementParameter.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        control.replay();

        DataCenter model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(1);
        setUpVersionExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveStoragePool,
                StoragePoolParametersBase.class,
                new String[] { "StoragePoolId" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveForced() throws Exception {
        setUpGetEntityExpectations(1);
        setUpVersionExpectations();
        UriInfo uriInfo = setUpActionExpectations(
            VdcActionType.RemoveStoragePool,
            StoragePoolParametersBase.class,
            new String[] { "StoragePoolId", "ForceDelete" },
            new Object[] { GUIDS[0], Boolean.TRUE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendDataCenterResource.FORCE, Boolean.TRUE.toString());
        setUriInfo(uriInfo);
        control.replay();
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveForcedIncomplete() throws Exception {
        setUpGetEntityExpectations(1);
        setUpVersionExpectations();
        UriInfo uriInfo = setUpActionExpectations(
            VdcActionType.RemoveStoragePool,
            StoragePoolParametersBase.class,
            new String[] { "StoragePoolId", "ForceDelete" },
            new Object[] { GUIDS[0], Boolean.FALSE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendDataCenterResource.FORCE, Boolean.FALSE.toString());
        setUriInfo(uriInfo);
        control.replay();
        resource.remove();
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
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
        setUpGetEntityExpectations(1);
        setUpVersionExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveStoragePool,
                StoragePoolParametersBase.class,
                new String[] { "StoragePoolId" },
                new Object[] { GUIDS[0] },
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

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetStoragePoolById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : getEntity(0));
        }
    }

    protected void setUpVersionExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetAvailableStoragePoolVersions,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   getVersions());
    }

    @Override
    protected StoragePool getEntity(int index) {
        return setUpEntityExpectations(control.createMock(StoragePool.class), index);
    }

    protected List<Version> getVersions() {
        Version version = control.createMock(Version.class);
        expect(version.getMajor()).andReturn(2);
        expect(version.getMinor()).andReturn(3);
        List<Version> versions = new ArrayList<>();
        versions.add(version);
        return versions;
    }

    @Override
    protected void verifyModel(DataCenter model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }
}
