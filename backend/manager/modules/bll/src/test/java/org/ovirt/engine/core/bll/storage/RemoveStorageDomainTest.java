package org.ovirt.engine.core.bll.storage;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.ArrayList;

import org.junit.Test;
import org.ovirt.engine.core.bll.BaseMockitoTest;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RemoveVGVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainDynamicDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VdsDAO;

public class RemoveStorageDomainTest extends BaseMockitoTest {

    private Guid STORAGE_DOMAIN_ID = GUIDS[0];
    private Guid STORAGE_POOL_ID = GUIDS[1];
    private Guid VDS_ID = GUIDS[2];

    @Test
    public void testCanDoAction() {
        DbFacade db = setUpDB();
        expectGetStoragePool(db, STORAGE_POOL_ID);
        expectGetStorageDomain(db,
                               STORAGE_DOMAIN_ID,
                               STORAGE_POOL_ID,
                               StorageDomainType.Data,
                               StorageType.NFS);
        expectGetVds(db, VDS_ID);
        expectGetIsoMap(db, STORAGE_DOMAIN_ID);

        expectBusinessEntitySnapshotDAO(db);

        RemoveStorageDomainCommand cmd = new RemoveStorageDomainCommand(getParams(true));
        assertTrue(cmd.canDoAction());

        checkSucceeded(cmd, false);
        checkMessages(cmd,
                      VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN,
                      VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Test
    public void testRemoveNfsData() {
        doTestRemove(StorageDomainType.Data, StorageType.NFS, true, false);
    }

    @Test
    public void testRemoveIscsiData() {
        doTestRemove(StorageDomainType.Data, StorageType.ISCSI, true, false);
    }

    @Test
    public void testRemoveAndFormatNfsIso() {
        doTestRemove(StorageDomainType.ISO, StorageType.NFS, true, false);
    }

    @Test
    public void testRemoveNfsExport() {
        doTestRemove(StorageDomainType.ImportExport, StorageType.NFS, false, false);
    }

    @Test
    public void testRemoveLocalData() {
        doTestRemove(StorageDomainType.Data, StorageType.LOCALFS, true, false);
    }

    @Test
    public void testRemoveFormatFailed() {
        doTestRemove(StorageDomainType.Data, StorageType.NFS, true, true);
    }

    public void doTestRemove(StorageDomainType type, StorageType storageType, boolean format, boolean failure) {
        DbFacade db = setUpDB();
        expectGetStoragePool(db, STORAGE_POOL_ID);
        storage_domains dom = expectGetStorageDomain(db,
                                                     STORAGE_DOMAIN_ID,
                                                     STORAGE_POOL_ID,
                                                     type,
                                                     storageType);
        expectGetVds(db, VDS_ID);

        BackendInternal backend = null;

        if (storageType == StorageType.LOCALFS) {
            expectGetIsAttached(db, STORAGE_DOMAIN_ID, STORAGE_POOL_ID);
            if (backend == null) {
                backend = setUpBackend();
            }
            expectDetach(backend, STORAGE_DOMAIN_ID, STORAGE_POOL_ID);
        }

        if (format || (type != StorageDomainType.ISO && type != StorageDomainType.ImportExport)) {
            setUpStorageHelper(dom, storageType, true, failure);

            if (backend == null) {
                backend = setUpBackend();
            }

            VDSBrokerFrontend vdsBroker = expectFormat(setUpVdsBroker(backend), failure);

            if (storageType == StorageType.ISCSI) {
                expectRemove(vdsBroker);
            }
        } else {
            setUpStorageHelper(dom, storageType, false, false);
        }

        if (!failure) {
            expectRemoveFromDb(db, STORAGE_DOMAIN_ID);
        }

        expectBusinessEntitySnapshotDAO(db);

        RemoveStorageDomainCommand cmd = new RemoveStorageDomainCommand(getParams(format));
        cmd.executeCommand();

        checkSucceeded(cmd, !failure);
        checkMessages(cmd);
    }

    protected VDSBrokerFrontend setUpVdsBroker(BackendInternal backend) {
        VDSBrokerFrontend vdsBroker = mock(VDSBrokerFrontend.class);
        when(backend.getResourceManager()).thenReturn(vdsBroker);
        return vdsBroker;
    }

    protected IStorageHelper setUpStorageHelper(storage_domains dom,
                                                StorageType storageType,
                                                boolean connect,
                                                boolean failure) {
        StorageHelperDirector director = mock(StorageHelperDirector.class);
        when(StorageHelperDirector.getInstance()).thenReturn(director);
        IStorageHelper helper = mock(IStorageHelper.class);
        if (connect) {
            when(helper.ConnectStorageToDomainByVdsId(dom, VDS_ID)).thenReturn(true);
            when(helper.DisconnectStorageFromDomainByVdsId(dom, VDS_ID)).thenReturn(true);
        }
        if (!failure) {
            when(helper.StorageDomainRemoved(dom.getStorageStaticData())).thenReturn(true);
        }
        when(director.getItem(storageType)).thenReturn(helper);
        return helper;
    }

    protected RemoveStorageDomainParameters getParams(boolean format) {
        RemoveStorageDomainParameters params = new RemoveStorageDomainParameters(STORAGE_DOMAIN_ID);
        params.setStoragePoolId(STORAGE_POOL_ID);
        params.setVdsId(VDS_ID);
        params.setDoFormat(format);
        return params;
    }

    protected storage_domains expectGetStorageDomain(DbFacade db,
                                                     Guid domId,
                                                     Guid poolId,
                                                     StorageDomainType type,
                                                     StorageType storageType) {
        StorageDomainDAO dao = mock(StorageDomainDAO.class);
        when(db.getStorageDomainDAO()).thenReturn(dao);
        storage_domains dom = getStorageDomain(domId, poolId, type, storageType);
        when(dao.getForStoragePool(domId, poolId)).thenReturn(dom);
        return dom;
    }

    protected void expectGetStorageDomainStatic(DbFacade db, Guid domId) {
        StorageDomainStaticDAO dao = mock(StorageDomainStaticDAO.class);
        when(db.getStorageDomainStaticDAO()).thenReturn(dao);
        when(dao.get(domId)).thenReturn(getStorageDomainStatic(domId));
    }

    protected void expectGetStoragePool(DbFacade db, Guid id) {
        StoragePoolDAO dao = mock(StoragePoolDAO.class);
        when(db.getStoragePoolDAO()).thenReturn(dao);
        when(dao.get(id)).thenReturn(getStoragePool(id));
    }

    protected void expectGetIsoMap(DbFacade db, Guid id) {
        ArrayList<storage_pool_iso_map> ret = new ArrayList<storage_pool_iso_map>();
        StoragePoolIsoMapDAO dao = mock(StoragePoolIsoMapDAO.class);
        when(db.getStoragePoolIsoMapDAO()).thenReturn(dao);
        when(dao.getAllForStorage(id)).thenReturn(ret);
    }

    protected void expectGetVds(DbFacade db, Guid id) {
        VdsDAO dao = mock(VdsDAO.class);
        when(db.getVdsDAO()).thenReturn(dao);
        when(dao.get(id)).thenReturn(getVds(id));
    }

    protected void expectGetIsAttached(DbFacade db, Guid id, Guid poolId) {
        StoragePoolIsoMapDAO dao = mock(StoragePoolIsoMapDAO.class);
        when(db.getStoragePoolIsoMapDAO()).thenReturn(dao);
        when(dao.get(new StoragePoolIsoMapId(id, poolId))).thenReturn(new storage_pool_iso_map());
    }

    protected void expectDetach(BackendInternal backend, Guid id, Guid poolId) {
        VdcReturnValueBase ret = new VdcReturnValueBase();
        ret.setSucceeded(true);
        when(backend.runInternalAction(eq(VdcActionType.DetachStorageDomainFromPool),
                                         any(DetachStorageDomainFromPoolParameters.class))).thenReturn(ret);
    }

    protected VDSBrokerFrontend expectFormat(VDSBrokerFrontend vdsBroker, boolean failure) {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(!failure);
        when(vdsBroker.RunVdsCommand(eq(VDSCommandType.FormatStorageDomain),
                                       any(FormatStorageDomainVDSCommandParameters.class))).thenReturn(ret);
        return vdsBroker;
    }

    protected VDSBrokerFrontend expectRemove(VDSBrokerFrontend vdsBroker) {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(true);
        when(vdsBroker.RunVdsCommand(eq(VDSCommandType.RemoveVG),
                                       any(RemoveVGVDSCommandParameters.class))).thenReturn(ret);
        return vdsBroker;
    }

    protected void expectBusinessEntitySnapshotDAO(DbFacade db) {
        BusinessEntitySnapshotDAO dao = mock(BusinessEntitySnapshotDAO.class);
        when(db.getBusinessEntitySnapshotDAO()).thenReturn(dao);
    }

    protected void expectRemoveStaticFromDb(DbFacade db, Guid id) {
        StorageDomainStaticDAO dao = mock(StorageDomainStaticDAO.class);
        when(db.getStorageDomainStaticDAO()).thenReturn(dao);
        // dao.remove(id);
    }

    protected void expectRemoveDynamicFromDb(DbFacade db, Guid id) {
        StorageDomainDynamicDAO dao = mock(StorageDomainDynamicDAO.class);
        when(db.getStorageDomainDynamicDAO()).thenReturn(dao);
        // dao.remove(id);
    }

    protected void expectRemoveFromDb(DbFacade db, Guid id) {
        expectRemoveDynamicFromDb(db, id);
        expectRemoveStaticFromDb(db, id);
    }

    protected storage_domains getStorageDomain(Guid id,
                                               Guid poolId,
                                               StorageDomainType type,
                                               StorageType storageType) {
        storage_domains dom = new storage_domains();
        dom.setid(id);
        dom.setstorage_pool_id(poolId);
        dom.setstorage_domain_type(type);
        dom.setstorage_type(storageType);
        dom.setstorage_domain_shared_status(StorageDomainSharedStatus.Unattached);
        return dom;
    }

    protected storage_domain_static getStorageDomainStatic(Guid id) {
        storage_domain_static dom = new storage_domain_static();
        dom.setId(id);
        return dom;
    }

    protected storage_pool getStoragePool(Guid id) {
        storage_pool pool = new storage_pool();
        pool.setId(id);
        return pool;
    }

    protected VDS getVds(Guid id) {
        VDS vds = new VDS();
        vds.setvds_id(id);
        return vds;
    }
}
