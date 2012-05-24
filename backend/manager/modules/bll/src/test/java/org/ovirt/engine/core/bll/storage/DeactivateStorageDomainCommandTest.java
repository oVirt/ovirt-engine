package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ExecuteTransactionAnswer;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
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
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@RunWith(MockitoJUnitRunner.class)
public class DeactivateStorageDomainCommandTest {

    @Mock
    private DbFacade dbFacade;
    @Mock
    private StoragePoolIsoMapDAO isoMapDAO;
    @Mock
    private StoragePoolDAO storagePoolDAO;
    @Mock
    private StorageDomainDAO storageDomainDAO;
    @Mock
    private VdsDAO vdsDAO;
    @Mock
    private BackendInternal backendInternal;
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;
    @Mock
    private VDS vds;
    storage_pool_iso_map map = new storage_pool_iso_map();

    @Test
    public void statusSetInMap() {
        StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(Guid.NewGuid(), Guid.NewGuid());
        DeactivateStorageDomainCommand<StorageDomainPoolParametersBase> cmd =
                spy(new DeactivateStorageDomainCommand<StorageDomainPoolParametersBase>(params));

        doReturn(mock(IStorageHelper.class)).when(cmd).getStorageHelper(any(storage_domains.class));
        doReturn(dbFacade).when(cmd).getDbFacade();

        when(dbFacade.getStoragePoolIsoMapDAO()).thenReturn(isoMapDAO);
        when(dbFacade.getStoragePoolDAO()).thenReturn(storagePoolDAO);
        when(dbFacade.getVdsDAO()).thenReturn(vdsDAO);
        when(dbFacade.getStorageDomainDAO()).thenReturn(storageDomainDAO);
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(new storage_pool());
        when(isoMapDAO.get(any(StoragePoolIsoMapId.class))).thenReturn(map);
        when(storageDomainDAO.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(new storage_domains());

        doAnswer(new ExecuteTransactionAnswer(0)).when(cmd).executeInNewTransaction(any(TransactionMethod.class));
        doAnswer(new ExecuteTransactionAnswer(1)).when(cmd).executeInScope(any(TransactionScopeOption.class),
                any(TransactionMethod.class));

        doReturn(backendInternal).when(cmd).getBackend();
        when(backendInternal.runInternalQuery(any(VdcQueryType.class), any(VdcQueryParametersBase.class)))
                .thenReturn(new VdcQueryReturnValue());
        when(backendInternal.getResourceManager()).thenReturn(vdsBrokerFrontend);
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        when(vdsBrokerFrontend.RunVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class)))
                .thenReturn(returnValue);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
        map.setstatus(StorageDomainStatus.Active);

        cmd.setCompensationContext(mock(CompensationContext.class));
        cmd.executeCommand();
        assertTrue(map.getstatus() == StorageDomainStatus.Maintenance);
    }
}
