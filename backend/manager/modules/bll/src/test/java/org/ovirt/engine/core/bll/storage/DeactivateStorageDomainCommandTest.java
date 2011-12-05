package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.MockitoAnnotations.Mock;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({DbFacade.class, LogFactory.class, EjbUtils.class, Backend.class, StorageHelperDirector.class})
@RunWith(PowerMockRunner.class)
public class DeactivateStorageDomainCommandTest {

    @Mock private DbFacade dbFacade;
    @Mock private StoragePoolIsoMapDAO isoMapDAO;
    @Mock private StoragePoolDAO storagePoolDAO;
    @Mock private StorageDomainDAO storageDomainDAO;
    @Mock private VdsDAO vdsDAO;
    @Mock private TransactionManager manager;
    @Mock private BackendInternal  backendInternal;
    @Mock private VDSBrokerFrontend vdsBrokerFrontend;
    @Mock private StorageHelperDirector storageHelperDirector;
    @Mock private VDS vds;
    storage_pool_iso_map map = new storage_pool_iso_map();

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(LogFactory.class);
        mockStatic(EjbUtils.class);
        mockStatic(Backend.class);
        mockStatic(StorageHelperDirector.class);
        when(DbFacade.getInstance()).thenReturn(dbFacade);
        when(LogFactory.getLog(any(Class.class))).thenReturn(mock(Log.class));
        when(EjbUtils.findResource(any(ContainerManagedResourceType.class))).thenReturn(manager);
        when(Backend.getInstance()).thenReturn(backendInternal);
        when(StorageHelperDirector.getInstance()).thenReturn(storageHelperDirector);
    }

    @Test
    public void statusSetInMap() throws SystemException {
        when(dbFacade.getStoragePoolIsoMapDAO()).thenReturn(isoMapDAO);
        when(dbFacade.getStoragePoolDAO()).thenReturn(storagePoolDAO);
        when(dbFacade.getVdsDAO()).thenReturn(vdsDAO);
        when(dbFacade.getStorageDomainDAO()).thenReturn(storageDomainDAO);
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(new storage_pool());
        when(isoMapDAO.get(any(StoragePoolIsoMapId.class))).thenReturn(map);
        when(storageDomainDAO.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(new storage_domains());
        when(manager.getTransaction()).thenReturn(mock(Transaction.class));
        when(backendInternal.runInternalQuery(any(VdcQueryType.class), any(VdcQueryParametersBase.class)))
                .thenReturn(new VdcQueryReturnValue());
        when(backendInternal.getResourceManager()).thenReturn(vdsBrokerFrontend);
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        when(vdsBrokerFrontend.RunVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class)))
                .thenReturn(returnValue);
        when(storageHelperDirector.getItem(any(StorageType.class))).thenReturn(mock(IStorageHelper.class));
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
        map.setstatus(StorageDomainStatus.Active);
        StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(Guid.NewGuid(), Guid.NewGuid());
        DeactivateStorageDomainCommand<StorageDomainPoolParametersBase> cmd =
                new DeactivateStorageDomainCommand<StorageDomainPoolParametersBase>(params);
        cmd.setCompensationContext(mock(CompensationContext.class));
        cmd.executeCommand();
        assertTrue(map.getstatus() == StorageDomainStatus.Maintenance);
    }
}
