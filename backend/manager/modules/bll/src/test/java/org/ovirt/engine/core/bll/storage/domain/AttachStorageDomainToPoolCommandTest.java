package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.AttachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

public class AttachStorageDomainToPoolCommandTest extends BaseCommandTest {
    @Mock
    private StoragePoolIsoMapDao isoMapDao;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;
    @Mock
    private DiskImageDao diskImageDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private BackendInternal backendInternal;
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;
    @Mock
    private VDS vds;
    private StoragePoolIsoMap map;

    @ClassRule
    public static MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule();

    @Test
    public void statusSetInMap() {
        Guid storageDomainId = Guid.newGuid();
        Guid poolId = Guid.newGuid();
        AttachStorageDomainToPoolParameters params =
                new AttachStorageDomainToPoolParameters(storageDomainId, poolId);
        AttachStorageDomainToPoolCommand<AttachStorageDomainToPoolParameters> cmd =
                spy(new AttachStorageDomainToPoolCommand<>(
                        params, CommandContext.createContext(params.getSessionId())));

        doNothing().when(cmd).attemptToActivateDomain();
        doReturn(Collections.emptyList()).when(cmd).connectHostsInUpToDomainStorageServer();
        doReturn(isoMapDao).when(cmd).getStoragePoolIsoMapDao();
        doReturn(storagePoolDao).when(cmd).getStoragePoolDao();
        doReturn(vdsDao).when(cmd).getVdsDao();
        doReturn(storageDomainDao).when(cmd).getStorageDomainDao();
        doReturn(storageDomainStaticDao).when(cmd).getStorageDomainStaticDao();
        doReturn(diskImageDao).when(cmd).getDiskImageDao();
        doReturn(vdsBrokerFrontend).when(cmd).getVdsBroker();

        StoragePool pool = new StoragePool();
        pool.setId(poolId);
        pool.setStatus(StoragePoolStatus.Up);
        when(storagePoolDao.get(any(Guid.class))).thenReturn(pool);
        when(isoMapDao.get(any(StoragePoolIsoMapId.class))).thenReturn(map);
        when(storageDomainDao.getForStoragePool(any(Guid.class), any(Guid.class))).thenReturn(new StorageDomain());
        when(diskImageDao.getAllForStorageDomain(any(Guid.class))).thenReturn(Collections.EMPTY_LIST);
        when(storageDomainStaticDao.get(any(Guid.class))).thenReturn(new StorageDomainStatic());
        doReturn(pool.getId()).when(cmd).getStoragePoolIdFromVds();
        doReturn(backendInternal).when(cmd).getBackend();
        VdcReturnValueBase vdcReturnValue = new VdcReturnValueBase();
        vdcReturnValue.setSucceeded(true);
        when(backendInternal.runInternalAction(any(VdcActionType.class),
                any(VdcActionParametersBase.class),
                any(CommandContext.class))).thenReturn(vdcReturnValue);
        StorageDomainStatic storageDomain = new StorageDomainStatic();
        storageDomain.setId(Guid.newGuid());
        storageDomain.setStorageDomainType(StorageDomainType.ImportExport);
        mockGetStorageDomainInfoVdsCommand(storageDomain);
        mockAttachStorageDomainVdsCommand();
        when(vdsDao.get(any(Guid.class))).thenReturn(vds);
        doReturn(Collections.EMPTY_LIST).when(cmd).getEntitiesFromStorageOvfDisk(storageDomainId, pool.getId());
        doReturn(Collections.EMPTY_LIST).when(cmd).getAllOVFDisks(storageDomainId, pool.getId());
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                map = (StoragePoolIsoMap) invocation.getArguments()[0];
                return null;
            }
        }).when(isoMapDao).save(any(StoragePoolIsoMap.class));

        cmd.setCompensationContext(mock(CompensationContext.class));
        cmd.executeCommand();
        assertNotNull(map);
        assertEquals(StorageDomainStatus.Maintenance, map.getStatus());
    }

    private void mockAttachStorageDomainVdsCommand() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.AttachStorageDomain),
                any(AttachStorageDomainVDSCommandParameters.class))).thenReturn(returnValue);
    }

    private void mockGetStorageDomainInfoVdsCommand(StorageDomainStatic storageDomain) {
        Pair<StorageDomainStatic, Guid> pairResult = new Pair<>(storageDomain, null);
        VDSReturnValue returnValueForGetStorageDomainInfo = new VDSReturnValue();
        returnValueForGetStorageDomainInfo.setSucceeded(true);
        returnValueForGetStorageDomainInfo.setReturnValue(pairResult);
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.HSMGetStorageDomainInfo),
                any(HSMGetStorageDomainInfoVDSCommandParameters.class))).thenReturn(returnValueForGetStorageDomainInfo);
    }
}
