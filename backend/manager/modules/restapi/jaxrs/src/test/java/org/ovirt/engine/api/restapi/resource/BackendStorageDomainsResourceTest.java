package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.LogicalUnits;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainsResourceTest
        extends AbstractBackendCollectionResourceTest<StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain, BackendStorageDomainsResource> {

    protected static final StorageDomainType[] TYPES = { StorageDomainType.DATA,
            StorageDomainType.ISO, StorageDomainType.EXPORT };
    protected static final StorageType[] STORAGE_TYPES = { StorageType.NFS, StorageType.NFS,
            StorageType.LOCALFS, StorageType.POSIXFS };

    protected static final int LOCAL_IDX = 2;
    protected static final int POSIX_IDX = 3;

    protected static final String[] ADDRESSES = { "10.11.12.13", "13.12.11.10", "10.01.10.01", "10.01.10.14" };
    protected static final String[] PATHS = { "/1", "/2", "/3", "/4" };
    protected static final String[] MOUNT_OPTIONS = { "", "", "", "rw" };
    protected static final String[] VFS_TYPES = { "", "", "", "nfs" };
    protected static final String LUN = "1IET_00010001";
    protected static final String TARGET = "iqn.2009-08.org.fubar.engine:markmc.test1";
    protected static final Integer PORT = 3260;

    protected static final org.ovirt.engine.core.common.businessentities.StorageDomainType[] TYPES_MAPPED = {
            org.ovirt.engine.core.common.businessentities.StorageDomainType.Data,
            org.ovirt.engine.core.common.businessentities.StorageDomainType.ISO,
            org.ovirt.engine.core.common.businessentities.StorageDomainType.ImportExport };

    protected static final org.ovirt.engine.core.common.businessentities.storage.StorageType[] STORAGE_TYPES_MAPPED = {
            org.ovirt.engine.core.common.businessentities.storage.StorageType.NFS,
            org.ovirt.engine.core.common.businessentities.storage.StorageType.NFS,
            org.ovirt.engine.core.common.businessentities.storage.StorageType.LOCALFS,
            org.ovirt.engine.core.common.businessentities.storage.StorageType.POSIXFS };

    public BackendStorageDomainsResourceTest() {
        super(new BackendStorageDomainsResource(), SearchType.StorageDomain, "Storage : ");
    }

    @Test
    public void testAddStorageDomain() {
        Host host = new Host();
        host.setId(GUIDS[0].toString());
        doTestAddStorageDomain(0, host, false);
    }

    @Test
    public void testAddStorageDomainWithExistingConnectionId() {
        Host host = new Host();
        host.setId(GUIDS[0].toString());
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(QueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[POSIX_IDX].toString() },
                setUpPosixStorageServerConnection(POSIX_IDX));
        setUpGetEntityExpectations(QueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[POSIX_IDX].toString() },
                setUpPosixStorageServerConnection(POSIX_IDX));
        setUpGetEntityExpectations(QueryType.GetExistingStorageDomainList,
                GetExistingStorageDomainListParameters.class,
                new String[] { "Id", "StorageType", "StorageDomainType", "Path" },
                new Object[] { GUIDS[0], STORAGE_TYPES_MAPPED[POSIX_IDX], TYPES_MAPPED[0], ADDRESSES[POSIX_IDX] + ":" + PATHS[POSIX_IDX] },
                new ArrayList<>());

        setUpCreationExpectations(ActionType.AddPosixFsStorageDomain,
                StorageDomainManagementParameter.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] },
                true,
                true,
                GUIDS[POSIX_IDX],
                QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[POSIX_IDX] },
                getEntity(POSIX_IDX));

        StorageDomain model = new StorageDomain();
        model.setName(getSafeEntry(POSIX_IDX, NAMES));
        model.setDescription(getSafeEntry(POSIX_IDX, DESCRIPTIONS));
        model.setType(getSafeEntry(POSIX_IDX, TYPES));
        model.setStorage(new HostStorage());
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());
        model.getStorage().setId(GUIDS[POSIX_IDX].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), POSIX_IDX);
    }

    @Test
    public void testAddStorageDomainWithNoStorageObject() {
        Host host = new Host();
        host.setId(GUIDS[0].toString());
        setUriInfo(setUpBasicUriExpectations());
        StorageDomain model = new StorageDomain();
        model.setName(getSafeEntry(POSIX_IDX, NAMES));
        model.setDescription(getSafeEntry(POSIX_IDX, DESCRIPTIONS));
        model.setType(getSafeEntry(POSIX_IDX, TYPES));
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());

        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "StorageDomain", "add", "storage");
    }

    @Test
    public void testAddStorageDomainWithHostName() {
        Host host = new Host();
        host.setName(NAMES[0]);

        setUpGetEntityExpectations(QueryType.GetVdsStaticByName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[0] },
                setUpVDStatic(0));

        doTestAddStorageDomain(0, host, false);
    }

    @Test
    public void testAddExistingStorageDomain() {
        Host host = new Host();
        host.setId(GUIDS[0].toString());
        doTestAddStorageDomain(1, host, true);
    }

    public void doTestAddStorageDomain(int idx, Host host, boolean existing) {
        setUriInfo(setUpActionExpectations(ActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.Connection", "StorageServerConnection.StorageType", "VdsId" },
                new Object[] { ADDRESSES[idx] + ":" + PATHS[idx], STORAGE_TYPES_MAPPED[idx], GUIDS[0] },
                true,
                true,
                GUIDS[idx].toString()));

        setUpGetEntityExpectations(QueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[idx].toString() },
                setUpStorageServerConnection(idx));

        setUpGetEntityExpectations(QueryType.GetExistingStorageDomainList,
                GetExistingStorageDomainListParameters.class,
                new String[] { "Id", "StorageType", "StorageDomainType", "Path" },
                new Object[] { GUIDS[0], STORAGE_TYPES_MAPPED[idx], TYPES_MAPPED[idx],
                        ADDRESSES[idx] + ":" + PATHS[idx] },
                getExistingStorageDomains(existing));

        setUpCreationExpectations(!existing ? ActionType.AddNFSStorageDomain
                : ActionType.AddExistingFileStorageDomain,
                StorageDomainManagementParameter.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[idx],
                QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[idx] },
                getEntity(idx));

        StorageDomain model = getModel(idx);
        model.setHost(host);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), idx);
    }

    @Test
    public void testAddLocalStorageDomain() {
        setUriInfo(setUpActionExpectations(ActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.Connection", "StorageServerConnection.StorageType", "VdsId" },
                new Object[] { PATHS[LOCAL_IDX], STORAGE_TYPES_MAPPED[LOCAL_IDX], GUIDS[0] },
                true,
                true,
                GUIDS[LOCAL_IDX].toString()));

        setUpGetEntityExpectations(QueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[LOCAL_IDX].toString() },
                setUpLocalStorageServerConnection(LOCAL_IDX));

        setUpCreationExpectations(ActionType.AddLocalStorageDomain,
                StorageDomainManagementParameter.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] },
                true,
                true,
                GUIDS[LOCAL_IDX],
                QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[LOCAL_IDX] },
                getEntity(LOCAL_IDX));

        StorageDomain model = getModel(LOCAL_IDX);
        model.getStorage().setAddress(null);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), LOCAL_IDX);
    }

    @Test
    public void testAddPosixStorageDomain() {
        setUriInfo(setUpActionExpectations(ActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.Connection",
                        "StorageServerConnection.StorageType",
                        "StorageServerConnection.MountOptions",
                        "StorageServerConnection.VfsType",
                        "VdsId" },
                new Object[] { ADDRESSES[POSIX_IDX] + ":" + PATHS[POSIX_IDX],
                        STORAGE_TYPES_MAPPED[POSIX_IDX],
                        MOUNT_OPTIONS[POSIX_IDX], VFS_TYPES[POSIX_IDX], GUIDS[0] },
                true,
                true,
                GUIDS[POSIX_IDX].toString()));

        setUpGetEntityExpectations(QueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[POSIX_IDX].toString() },
                setUpPosixStorageServerConnection(POSIX_IDX));

        setUpGetEntityExpectations(QueryType.GetExistingStorageDomainList,
                GetExistingStorageDomainListParameters.class,
                new String[] { "Id", "StorageType", "StorageDomainType", "Path" },
                new Object[] { GUIDS[0], STORAGE_TYPES_MAPPED[POSIX_IDX], TYPES_MAPPED[0], ADDRESSES[POSIX_IDX] + ":" + PATHS[POSIX_IDX] },
                new ArrayList<>());

        setUpCreationExpectations(ActionType.AddPosixFsStorageDomain,
                StorageDomainManagementParameter.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] },
                true,
                true,
                GUIDS[POSIX_IDX],
                QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[POSIX_IDX] },
                getEntity(POSIX_IDX));

        StorageDomain model = getModel(POSIX_IDX);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), POSIX_IDX);
    }

    @Test
    public void testAddIscsiStorageDomain() {
        StorageDomain model = getIscsi();

        Host host = new Host();
        host.setId(GUIDS[0].toString());
        model.setHost(host);

        setUriInfo(setUpActionExpectations(ActionType.ConnectStorageToVds,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.Connection", "VdsId" },
                new Object[] { ADDRESSES[0], GUIDS[0] },
                true,
                true,
                GUIDS[0].toString()));

        setUpGetEntityExpectations(QueryType.GetDeviceList,
                GetDeviceListQueryParameters.class,
                new String[] { "Id", "StorageType" },
                new Object[] { GUIDS[0], org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI },
                "this return value isn't used");

        setUpGetEntityExpectations(QueryType.GetLunsByVgId,
                GetLunsByVgIdParameters.class,
                new String[] { "VgId" },
                new Object[] { GUIDS[GUIDS.length - 1].toString() },
                setUpLuns());

        setUpCreationExpectations(ActionType.AddSANStorageDomain,
                AddSANStorageDomainParameters.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] },
                true,
                true,
                GUIDS[0],
                QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getIscsiEntity());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyIscsi((StorageDomain) response.getEntity());
    }

    @Test
    public void testAddIscsiStorageDomainAssumingConnection() {
        StorageDomain model = getIscsi();

        Host host = new Host();
        host.setId(GUIDS[0].toString());
        model.setHost(host);
        for (LogicalUnit lun : model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits()) {
            lun.setAddress(null);
            lun.setTarget(null);
        }
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(QueryType.GetDeviceList,
                GetDeviceListQueryParameters.class,
                new String[] { "Id", "StorageType" },
                new Object[] { GUIDS[0], org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI },
                "this return value isn't used");

        List<LUNs> luns = setUpLuns();
        setUpGetEntityExpectations(QueryType.GetLunsByVgId,
                GetLunsByVgIdParameters.class,
                new String[] { "VgId" },
                new Object[] { GUIDS[GUIDS.length - 1].toString() },
                luns);

        setUpCreationExpectations(ActionType.AddSANStorageDomain,
                AddSANStorageDomainParameters.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] },
                true,
                true,
                GUIDS[0],
                QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getIscsiEntity());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyIscsi((StorageDomain) response.getEntity());
    }

    @Test
    public void testAddStorageDomainNoHost() {
        setUriInfo(setUpBasicUriExpectations());
        StorageDomain model = getModel(0);
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "StorageDomain", "add", "host.id|name");
    }

    @Test
    public void testAddStorageDomainCantDo() {
        doTestBadAddStorageDomain(false, true, CANT_DO);
    }

    @Test
    public void testAddStorageDomainFailure() {
        doTestBadAddStorageDomain(true, false, FAILURE);
    }

    private void doTestBadAddStorageDomain(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.Connection", "StorageServerConnection.StorageType", "VdsId" },
                new Object[] { ADDRESSES[0] + ":" + PATHS[0], STORAGE_TYPES_MAPPED[0], GUIDS[0] },
                true,
                true,
                GUIDS[0].toString()));

        setUpActionExpectations(ActionType.RemoveStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] {},
                new Object[] {},
                true,
                true, null);

        setUpGetEntityExpectations(QueryType.GetExistingStorageDomainList,
                GetExistingStorageDomainListParameters.class,
                new String[] { "Id", "StorageType", "StorageDomainType", "Path" },
                new Object[] { GUIDS[0], STORAGE_TYPES_MAPPED[0], TYPES_MAPPED[0], ADDRESSES[0] + ":" + PATHS[0] },
                new ArrayList<>());

        setUpActionExpectations(ActionType.AddNFSStorageDomain,
                StorageDomainManagementParameter.class,
                new String[] {},
                new Object[] {},
                valid,
                success);

        StorageDomain model = getModel(0);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddStorageDomainCantDoCnxAdd() {
        doTestBadCnxAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddStorageDomainCnxAddFailure() {
        doTestBadCnxAdd(true, false, FAILURE);
    }

    private void doTestBadCnxAdd(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.AddStorageServerConnection,
                StorageServerConnectionParametersBase.class,
                new String[] { "StorageServerConnection.Connection", "StorageServerConnection.StorageType", "VdsId" },
                new Object[] { ADDRESSES[0] + ":" + PATHS[0], STORAGE_TYPES_MAPPED[0], GUIDS[0] },
                valid,
                success,
                GUIDS[0].toString(),
                true));

        StorageDomain model = getModel(0);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteDomainParameters() {
        StorageDomain model = getModel(0);
        model.setName(NAMES[0]);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());
        model.setStorage(new HostStorage());
        model.getStorage().setAddress(ADDRESSES[0]);
        model.getStorage().setPath(PATHS[0]);
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "StorageDomain", "add", "storage.type");
    }

    @Test
    public void testAddIncompleteNfsStorageParameters() {
        StorageDomain model = getModel(0);
        model.setName(NAMES[0]);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());
        model.setStorage(new HostStorage());
        model.getStorage().setType(StorageType.NFS);
        model.getStorage().setPath(PATHS[0]);
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "HostStorage", "add", "address");
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        if (failure == null) {
            for (int i = 0; i < NAMES.length; i++) {
                setUpGetEntityExpectations(QueryType.GetStorageServerConnectionById,
                        StorageServerConnectionQueryParametersBase.class,
                        new String[] { "ServerConnectionId" },
                        new Object[] { GUIDS[i].toString() }, setUpStorageServerConnection(i));
            }
        }
        super.setUpQueryExpectations(query, failure);
    }

    static StorageServerConnections setUpLocalStorageServerConnection(int index) {
        return setUpStorageServerConnection(index, index, StorageType.LOCALFS);
    }

    static StorageServerConnections setUpPosixStorageServerConnection(int index) {
        return setUpStorageServerConnection(index, index, StorageType.POSIXFS);
    }

    static StorageServerConnections setUpStorageServerConnection(int index) {
        return setUpStorageServerConnection(index, index, StorageType.NFS);
    }

    static StorageServerConnections setUpStorageServerConnection(int idIndex, int index, StorageType storageType) {
        StorageServerConnections cnx = new StorageServerConnections();
        if (idIndex != -1) {
            cnx.setId(GUIDS[idIndex].toString());
        }
        if (storageType == StorageType.LOCALFS) {
            cnx.setConnection(PATHS[index]);
        } else if (storageType == StorageType.POSIXFS) {
            cnx.setConnection(ADDRESSES[index] + ":" + PATHS[index]);
            cnx.setMountOptions(MOUNT_OPTIONS[index]);
            cnx.setVfsType(VFS_TYPES[index]);
        } else {
            cnx.setConnection(ADDRESSES[index] + ":" + PATHS[index]);
        }
        cnx.setStorageType(STORAGE_TYPES_MAPPED[index]);
        return cnx;
    }

    protected VdsStatic setUpVDStatic(int index) {
        VdsStatic vds = new VdsStatic();
        vds.setId(GUIDS[index]);
        vds.setName(NAMES[index]);
        return vds;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.StorageDomain getEntity(int index) {
        return setUpEntityExpectations(mock(org.ovirt.engine.core.common.businessentities.StorageDomain.class),
                index);
    }

    static org.ovirt.engine.core.common.businessentities.StorageDomain setUpEntityExpectations(org.ovirt.engine.core.common.businessentities.StorageDomain entity,
            int index) {
        when(entity.getId()).thenReturn(getSafeEntry(index, GUIDS));
        when(entity.getStorageName()).thenReturn(getSafeEntry(index, NAMES));
        // REVIST No descriptions for storage domains
        // expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        when(entity.getStorageDomainType()).thenReturn(getSafeEntry(index, TYPES_MAPPED));
        when(entity.getStorageType()).thenReturn(getSafeEntry(index, STORAGE_TYPES_MAPPED));
        when(entity.getStorage()).thenReturn(getSafeEntry(index, GUIDS).toString());
        when(entity.getStorageStaticData()).thenReturn(new StorageDomainStatic());
        return entity;
    }

    private static <T> T getSafeEntry(int index, T[] arr) {
        return arr[index % arr.length];
    }

    protected List<LUNs> setUpLuns() {
        StorageServerConnections cnx = new StorageServerConnections();
        cnx.setConnection(ADDRESSES[0]);
        cnx.setIqn(TARGET);
        cnx.setPort(Integer.toString(PORT));

        LUNs lun = new LUNs();
        lun.setLUNId(LUN);
        lun.setLunConnections(new ArrayList<>());
        lun.getLunConnections().add(cnx);

        List<LUNs> luns = new ArrayList<>();
        luns.add(lun);
        return luns;
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain getIscsiEntity() {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity =
                mock(org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        when(entity.getId()).thenReturn(GUIDS[0]);
        when(entity.getStorageName()).thenReturn(NAMES[0]);
        when(entity.getStorageDomainType()).thenReturn(TYPES_MAPPED[0]);
        when(entity.getStorageType()).thenReturn(org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI);
        when(entity.getStorage()).thenReturn(GUIDS[GUIDS.length - 1].toString());
        when(entity.getStorageStaticData()).thenReturn(new StorageDomainStatic());
        return entity;
    }

    static StorageDomain getModel(int index) {
        StorageDomain model = new StorageDomain();
        model.setName(getSafeEntry(index, NAMES));
        model.setDescription(getSafeEntry(index, DESCRIPTIONS));
        model.setType(getSafeEntry(index, TYPES));
        model.setStorage(new HostStorage());
        model.getStorage().setType(getSafeEntry(index, STORAGE_TYPES));
        model.getStorage().setAddress(getSafeEntry(index, ADDRESSES));
        model.getStorage().setPath(getSafeEntry(index, PATHS));
        model.getStorage().setMountOptions(getSafeEntry(index, MOUNT_OPTIONS));
        model.getStorage().setVfsType(getSafeEntry(index, VFS_TYPES));
        return model;
    }

    protected List<org.ovirt.engine.core.common.businessentities.StorageDomain> getExistingStorageDomains(boolean existing) {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> ret =
                new ArrayList<>();
        if (existing) {
            ret.add(new org.ovirt.engine.core.common.businessentities.StorageDomain());
        }
        return ret;
    }

    @Override
    protected List<StorageDomain> getCollection() {
        return collection.list().getStorageDomains();
    }

    @Override
    protected void verifyModel(StorageDomain model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(StorageDomain model, int index) {
        assertEquals(getSafeEntry(index, GUIDS).toString(), model.getId());
        assertEquals(getSafeEntry(index, NAMES), model.getName());
        // REVIST No descriptions for storage domains
        // assertEquals(DESCRIPTIONS[index], model.getDescription());
        assertEquals(getSafeEntry(index, TYPES), model.getType());
        assertNotNull(model.getStorage());
        assertEquals(getSafeEntry(index, STORAGE_TYPES), model.getStorage().getType());
        if (index != LOCAL_IDX && index != POSIX_IDX) {
            assertEquals(getSafeEntry(index, ADDRESSES), model.getStorage().getAddress());
        }
        assertEquals(PATHS[index], model.getStorage().getPath());
        assertNotNull(getLinkByName(model, "permissions"));
        if (model.getType() == StorageDomainType.ISO) {
            assertEquals(5, model.getLinks().size());
            assertNotNull(getLinkByName(model, "files"));

        } else if (model.getType().equals(TYPES[2])) {
            assertEquals(7, model.getLinks().size());
            assertNotNull(getLinkByName(model, "templates"));
            assertNotNull(getLinkByName(model, "vms"));
        }
        assertNotNull(model.getLinks().get(0).getHref());
    }

    protected StorageDomain getIscsi() {
        StorageDomain model = getModel(0);
        model.getStorage().setType(StorageType.ISCSI);
        model.getStorage().setAddress(null);
        model.getStorage().setPath(null);
        model.getStorage().setVolumeGroup(new VolumeGroup());
        model.getStorage().getVolumeGroup().setLogicalUnits(new LogicalUnits());
        model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().add(new LogicalUnit());
        model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().get(0).setId(LUN);
        model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().get(0).setTarget(TARGET);
        model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().get(0).setAddress(ADDRESSES[0]);
        model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().get(0).setPort(PORT);
        model.getStorage().setOverrideLuns(false);
        return model;
    }

    protected void verifyIscsi(StorageDomain model) {
        assertEquals(GUIDS[0].toString(), model.getId());
        assertEquals(NAMES[0], model.getName());
        assertEquals(TYPES[0], model.getType());
        assertNotNull(model.getStorage());
        assertEquals(StorageType.ISCSI, model.getStorage().getType());
        assertNotNull(model.getStorage().getVolumeGroup());
        assertEquals(GUIDS[GUIDS.length - 1].toString(), model.getStorage().getVolumeGroup().getId());
        assertTrue(model.getStorage().getVolumeGroup().isSetLogicalUnits());
        assertNotNull(model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().get(0));
        assertEquals(LUN, model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().get(0).getId());
        assertEquals(TARGET, model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().get(0).getTarget());
        assertEquals(ADDRESSES[0], model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().get(0).getAddress());
        assertEquals(PORT, model.getStorage().getVolumeGroup().getLogicalUnits().getLogicalUnits().get(0).getPort());
        assertEquals(7, model.getLinks().size());
        assertNotNull(getLinkByName(model, "permissions"));
        assertNotNull(model.getLinks().get(0).getHref());
        verifyLinks(model);
    }
}
