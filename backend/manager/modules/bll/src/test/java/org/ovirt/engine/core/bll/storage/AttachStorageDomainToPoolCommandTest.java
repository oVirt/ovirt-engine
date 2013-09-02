package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class AttachStorageDomainToPoolCommandTest {
    @Mock
    private DbFacade dbFacade;
    @Mock
    private StoragePoolIsoMapDAO isoMapDAO;
    @Mock
    private StoragePoolDAO storagePoolDAO;
    @Mock
    private StorageDomainDAO storageDomainDAO;
    @Mock
    private StorageDomainStaticDAO storageDomainStaticDAO;
    @Mock
    private VdsDAO vdsDAO;
    @Mock
    private BackendInternal backendInternal;
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;
    @Mock
    private VDS vds;
    StoragePoolIsoMap map = null;

    @ClassRule
    public static MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule();

    @Test
    public void statusSetInMap() {
        StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(Guid.newGuid(), Guid.newGuid());
        AttachStorageDomainToPoolCommand<StorageDomainPoolParametersBase> cmd =
                spy(new AttachStorageDomainToPoolCommand<StorageDomainPoolParametersBase>(params));

        doReturn(dbFacade).when(cmd).getDbFacade();
        doNothing().when(cmd).attemptToActivateDomain();

        when(dbFacade.getStoragePoolIsoMapDao()).thenReturn(isoMapDAO);
        when(dbFacade.getStoragePoolDao()).thenReturn(storagePoolDAO);
        when(dbFacade.getVdsDao()).thenReturn(vdsDAO);
        when(dbFacade.getStorageDomainDao()).thenReturn(storageDomainDAO);
        when(dbFacade.getStorageDomainStaticDao()).thenReturn(storageDomainStaticDAO);
        StoragePool pool = new StoragePool();
        pool.setStatus(StoragePoolStatus.Up);
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(pool);
        when(isoMapDAO.get(any(StoragePoolIsoMapId.class))).thenReturn(map);
        when(storageDomainDAO.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(new StorageDomain());
        when(storageDomainStaticDAO.get(any(Guid.class))).thenReturn(new StorageDomainStatic());

        doReturn(backendInternal).when(cmd).getBackend();
        when(vdsDAO.getAllForStoragePoolAndStatus(any(Guid.class), any(VDSStatus.class))).thenReturn(new ArrayList<VDS>());
        when(backendInternal.getResourceManager()).thenReturn(vdsBrokerFrontend);
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        VdcReturnValueBase vdcReturnValue = new VdcReturnValueBase();
        vdcReturnValue.setSucceeded(true);
        when(backendInternal.runInternalAction(any(VdcActionType.class),
                any(VdcActionParametersBase.class),
                any(CommandContext.class))).thenReturn(vdcReturnValue);
        when(vdsBrokerFrontend.RunVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class)))
                .thenReturn(returnValue);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                map = (StoragePoolIsoMap) invocation.getArguments()[0];
                return null;
            }
        }).when(isoMapDAO).save(any(StoragePoolIsoMap.class));

        cmd.setCompensationContext(mock(CompensationContext.class));
        cmd.executeCommand();
        assertNotNull(map);
        assertEquals(StorageDomainStatus.Maintenance, map.getStatus());
    }
}
