package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.setUpStorageServerConnection;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResourceTest.verifyModelSpecific;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.LogicalUnits;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.ReduceSANStorageDomainDevicesCommandParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainResourceTest
        extends AbstractBackendSubResourceTest<StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain, BackendStorageDomainResource> {

    public BackendStorageDomainResourceTest() {
        super(new BackendStorageDomainResource(GUIDS[0].toString(), new BackendStorageDomainsResource()));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendStorageDomainResource("foo", null)));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true, getEntity(0));
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUpGetEntityExpectations(1, getEntity(0));
        setUpGetStorageServerConnectionExpectations(1);
        setUriInfo(setUpBasicUriExpectations());

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetFcp() {
        setUpGetEntityExpectations(1, getFcpEntity());
        setUpGetEntityExpectations(QueryType.GetLunsByVgId,
                GetLunsByVgIdParameters.class,
                new String[] { "VgId" },
                new Object[] { GUIDS[0].toString() },
                setUpLuns());
        setUriInfo(setUpBasicUriExpectations());
        verifyGetFcp(resource.get());
    }

    private void verifyGetFcp(StorageDomain model) {
        assertEquals(GUIDS[0].toString(), model.getId());
        assertEquals(NAMES[0], model.getName());
        assertEquals(StorageDomainType.DATA, model.getType());
        assertNotNull(model.getStorage());
        assertEquals(StorageType.FCP, model.getStorage().getType());
        assertNotNull(model.getLinks().get(0).getHref());
    }

    protected List<LUNs> setUpLuns() {
        LUNs lun = new LUNs();
        lun.setLUNId(GUIDS[2].toString());
        List<LUNs> luns = new ArrayList<>();
        luns.add(lun);
        return luns;
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain getFcpEntity() {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = mock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        when(entity.getId()).thenReturn(GUIDS[0]);
        when(entity.getStorageName()).thenReturn(NAMES[0]);
        when(entity.getStorageDomainType()).thenReturn(org.ovirt.engine.core.common.businessentities.StorageDomainType.Data);
        when(entity.getStorageType()).thenReturn(org.ovirt.engine.core.common.businessentities.storage.StorageType.FCP);
        when(entity.getStorage()).thenReturn(GUIDS[0].toString());
        when(entity.getStorageStaticData()).thenReturn(new StorageDomainStatic());
        return entity;
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true, getEntity(0));
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(2, getEntity(0));
        setUpGetStorageServerConnectionExpectations(2);

        setUriInfo(setUpActionExpectations(ActionType.UpdateStorageDomain,
                                           StorageDomainManagementParameter.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(1, getEntity(0));
        setUpGetStorageServerConnectionExpectations(1);

        setUriInfo(setUpActionExpectations(ActionType.UpdateStorageDomain,
                                           StorageDomainManagementParameter.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))), detail);
    }

    @Test
    public void testConflictedUpdate() {
        setUpGetEntityExpectations(1, getEntity(0));
        setUpGetStorageServerConnectionExpectations(1);
        setUriInfo(setUpBasicUriExpectations());

        StorageDomain model = getModel(1);
        model.setId(GUIDS[1].toString());
        verifyImmutabilityConstraint(assertThrows(WebApplicationException.class, () -> resource.update(model)));
    }

    @Test
    public void testRemoveStorageDomainNull() {
        setUpGetEntityExpectations();
        UriInfo uriInfo = setUpBasicUriExpectations();
        setUriInfo(uriInfo);
        verifyBadRequest(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveWithHostId() {
        setUpGetEntityExpectations();
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.RemoveStorageDomain,
            RemoveStorageDomainParameters.class,
            new String[] { "StorageDomainId", "VdsId", "DoFormat" },
            new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendStorageDomainResource.HOST, GUIDS[1].toString());
        setUriInfo(uriInfo);
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveWithFormat() {
        setUpGetEntityExpectations();
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.RemoveStorageDomain,
            RemoveStorageDomainParameters.class,
            new String[] { "StorageDomainId", "VdsId", "DoFormat" },
            new Object[] { GUIDS[0], GUIDS[1], Boolean.TRUE },
            true,
            true,
            false
        );
        Map<String, String> parameters = new HashMap<>();
        parameters.put(BackendStorageDomainResource.HOST, GUIDS[1].toString());
        parameters.put(BackendStorageDomainResource.FORMAT, Boolean.TRUE.toString());
        uriInfo = addMatrixParameterExpectations(uriInfo, parameters);
        setUriInfo(uriInfo);
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveWithDestroy() {
        setUpGetEntityExpectations();
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.ForceRemoveStorageDomain,
            StorageDomainParametersBase.class,
            new String[] { "StorageDomainId" },
            new Object[] { GUIDS[0] },
            true,
            true,
            false
        );
        Map<String, String> parameters = new HashMap<>();
        parameters.put(BackendStorageDomainResource.DESTROY, Boolean.TRUE.toString());
        uriInfo = addMatrixParameterExpectations(uriInfo, parameters);
        setUriInfo(uriInfo);
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveWithHostName() {
        setUpGetEntityExpectations();
        setUpGetEntityExpectations(
            QueryType.GetVdsStaticByName,
            NameQueryParameters.class,
            new String[] { "Name" },
            new Object[] { NAMES[1] },
            setUpVDStatic(1)
        );
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.RemoveStorageDomain,
            RemoveStorageDomainParameters.class,
            new String[] { "StorageDomainId", "VdsId", "DoFormat" },
            new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE },
            true,
            true,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendStorageDomainResource.HOST, NAMES[1]);
        setUriInfo(uriInfo);
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
        setUpGetEntityExpectations();
        UriInfo uriInfo = setUpActionExpectations(
            ActionType.RemoveStorageDomain,
            RemoveStorageDomainParameters.class,
            new String[] { "StorageDomainId", "VdsId", "DoFormat" },
            new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE },
            valid,
            success,
            false
        );
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendStorageDomainResource.HOST, GUIDS[1].toString());
        setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }
    protected void setUpGetEntityExpectations(int times, org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
        setUpGetEntityExpectations(times, false, entity);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound, org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetStorageDomainById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    protected void setUpGetStorageServerConnectionExpectations(int times) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetStorageServerConnectionById,
                                       StorageServerConnectionQueryParametersBase.class,
                                       new String[] { "ServerConnectionId" },
                                       new Object[] { GUIDS[0].toString() },
                                       setUpStorageServerConnection(0));
        }
    }

    private void setUpGetEntityExpectations() {
        setUpGetEntityExpectations(QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                new org.ovirt.engine.core.common.businessentities.StorageDomain());
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.StorageDomain getEntity(int index) {
        return setUpEntityExpectations(mock(org.ovirt.engine.core.common.businessentities.StorageDomain.class), index);
    }

    @Override
    protected void verifyModel(StorageDomain model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    protected VdsStatic setUpVDStatic(int index) {
        VdsStatic vds = new VdsStatic();
        vds.setId(GUIDS[index]);
        vds.setName(NAMES[index]);
        return vds;
    }

    @Test
    public void testRefreshLunsSize() {
        List<String> lunsArray = new ArrayList();
        lunsArray.add(GUIDS[2].toString());
        setUriInfo(setUpActionExpectations(ActionType.RefreshLunsSize,
                ExtendSANStorageDomainParameters.class,
                new String[]{"LunIds"},
                new Object[]{lunsArray},
                true,
                true));
        Action action = new Action();
        LogicalUnits luns= new LogicalUnits();
        LogicalUnit lun = new LogicalUnit();
        lun.setId(GUIDS[2].toString());
        luns.getLogicalUnits().add(lun);
        action.setLogicalUnits(luns);
        verifyActionResponse(resource.refreshLuns(action));
    }

    @Test
    public void reduceLuns() {
        List<String> paramsLuns = new LinkedList<>();
        paramsLuns.add(GUIDS[2].toString());
        paramsLuns.add(GUIDS[3].toString());
        setUriInfo(setUpActionExpectations(ActionType.ReduceSANStorageDomainDevices,
                ReduceSANStorageDomainDevicesCommandParameters.class,
                new String[]{"DevicesToReduce", "StorageDomainId"},
                new Object[]{paramsLuns, GUIDS[0]},
                true,
                true));
        Action action = new Action();
        LogicalUnits luns= new LogicalUnits();

        paramsLuns.forEach(s -> {
            LogicalUnit lun = new LogicalUnit();
            lun.setId(s);
            luns.getLogicalUnits().add(lun);
        });

        action.setLogicalUnits(luns);
        verifyActionResponse(resource.reduceLuns(action));
    }

    private void verifyActionResponse(Response response) {
        verifyActionResponse(response, "storagedomains/" + GUIDS[0], false);
    }
}
