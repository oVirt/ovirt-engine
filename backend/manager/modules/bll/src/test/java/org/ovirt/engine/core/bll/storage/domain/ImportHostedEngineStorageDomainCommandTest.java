package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.storage.domain.ImportHostedEngineStorageDomainCommand.SUPPORTED_DOMAIN_TYPES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class ImportHostedEngineStorageDomainCommandTest {

    private static final Guid HE_SP_ID = Guid.createGuidFromString("35000000-0000-0000-0000-000000000000");
    private static final Guid HE_SD_ID = Guid.createGuidFromString("35100000-0000-0000-0000-000000000000");
    private static final Guid HE_VDS_ID = Guid.createGuidFromString("35200000-0000-0000-0000-000000000000");
    private static final Guid VG_ID = Guid.createGuidFromString("35300000-0000-0000-0000-000000000000");
    private static final String ISCSIUSER = "iscsiuser";
    private static final String ISCSIPASS = "iscsipass";

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();
    @Mock
    private HostedEngineHelper hostedEngineHelper;
    private StorageDomainManagementParameter parameters = new StorageDomainManagementParameter();
    @InjectMocks @Spy
    private ImportHostedEngineStorageDomainCommand cmd = new ImportHostedEngineStorageDomainCommand(
            parameters, CommandContext.createContext(parameters.getSessionId()));
    @Mock
    private BackendInternal backend;
    @Mock
    private DbFacade dbFacade;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private BaseDiskDao baseDiskDao;
    @Mock
    private VDSBrokerFrontend vdsBroker;
    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private StorageServerConnectionDao storageServerConnectionDao;

    @Before
    public void initTest() {
        prepareCommand();
    }

    @Test
    public void failIfDcNotActive() throws Exception {
        mockGetExistingDomain(true);
        StoragePool pool = new StoragePool();
        pool.setStatus(StoragePoolStatus.Uninitialized);
        when(storagePoolDao.get(HE_SP_ID)).thenReturn(pool);

        cmd.init();
        cmd.validate();

        ValidateTestUtils.assertValidationMessages(
                "",
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_MASTER_STORAGE_DOMAIN_NOT_ACTIVE
        );
    }

    @Test
    public void failsIfImported() throws Exception {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(new StorageDomainStatic());
        mockGetExistingDomain(true);

        cmd.init();
        cmd.validate();

        ValidateTestUtils.assertValidationMessages(
                "",
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
        verify(cmd, times(0)).executeCommand();
    }

    @Test
    public void failsInNotImportedAndNotExists() throws Exception {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(null);
        mockGetExistingDomain(false);

        cmd.init();
        ValidateTestUtils.runAndAssertValidateFailure(
                "",
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        verify(backend, times(1)).runInternalQuery(
                eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));
        verify(cmd, times(0)).executeCommand();
    }

    @Test
    public void storageTypeUnsupported() {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(null);
        StorageDomain sd = mockGetExistingDomain(true);
        sd.setStorageType(StorageType.CINDER);
        sd.setId(HE_SD_ID);

        cmd.init();
        ValidateTestUtils.runAndAssertValidateFailure(
                "",
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED);
        verify(backend, times(1)).runInternalQuery(
                eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));
        verify(cmd, times(0)).executeCommand();
    }

    @Test
    public void validatePass() {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(null);
        StorageDomain sd = mockGetExistingDomain(true);
        int i = new Random().nextInt(SUPPORTED_DOMAIN_TYPES.length);
        sd.setStorageType(SUPPORTED_DOMAIN_TYPES[i]);
        sd.setId(HE_SD_ID);

        cmd.init();
        assertTrue(cmd.validate());
    }

    @Test
    public void callConcreteAddSD() {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(null);
        StorageDomain sd = mockGetExistingDomain(true);
        sd.setStorageType(StorageType.NFS);
        sd.setId(HE_SD_ID);
        mockCommandCall(VdcActionType.AddExistingFileStorageDomain, true);
        mockCommandCall(VdcActionType.AttachStorageDomainToPool, true);

        cmd.init();
        cmd.validate();
        cmd.executeCommand();

        verify(storageServerConnectionDao, times(1))
            .save(sd.getStorageStaticData().getConnection());
        verify(backend, times(1)).runInternalAction(
                eq(VdcActionType.AddExistingFileStorageDomain),
                any(VdcActionParametersBase.class),
                any(CommandContext.class));
    }

    @Test
    public void callConcreteAddBlockSD() {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(null);
        StorageDomain sd = mockGetExistingDomain(true);
        sd.setId(HE_SD_ID);
        sd.setStorageType(StorageType.ISCSI);
        sd.setStorage(VG_ID.toString());
        mockCommandCall(VdcActionType.RemoveDisk, false);
        mockCommandCall(VdcActionType.AddExistingBlockStorageDomain, true);
        doReturn(createVdsReturnValue(createSdLuns()))
                .when(vdsBroker).runVdsCommand(eq(VDSCommandType.GetDeviceList), any(VDSParametersBase.class));
        mockCommandCall(VdcActionType.AttachStorageDomainToPool, true);

        cmd.init();
        cmd.validate();
        cmd.executeCommand();

        verify(backend, times(1)).runInternalAction(
                eq(VdcActionType.RemoveDisk),
                any(VdcActionParametersBase.class));
        verify(backend, times(1)).runInternalAction(
                eq(VdcActionType.AddExistingBlockStorageDomain),
                any(VdcActionParametersBase.class),
                any(CommandContext.class));

        assertTrue(cmd.getReturnValue().getSucceeded());
        assertEquals(cmd.getReturnValue().getActionReturnValue(), sd);
        assertTrue(sd.getStorageStaticData().getConnection().getUserName().equals(ISCSIUSER));
        assertTrue(sd.getStorageStaticData().getConnection().getPassword().equals(ISCSIPASS));
        assertTrue(sd.getStorageStaticData().getStorage().equals(VG_ID.toString()));
    }

    protected ArrayList<LUNs> createSdLuns() {
        LUNs lun = new LUNs();
        lun.setVolumeGroupId(VG_ID.toString());
        lun.setStorageDomainId(HE_SD_ID);
        ArrayList<StorageServerConnections> connections = new ArrayList<>();
        StorageServerConnections connection = new StorageServerConnections();
        connection.setUserName(ISCSIUSER);
        connection.setPassword(ISCSIPASS);
        connections.add(connection);
        lun.setLunConnections(connections);

        ArrayList<LUNs> luns = new ArrayList<>();
        luns.add(lun);
        return luns;
    }

    private VDSReturnValue createVdsReturnValue(List<LUNs> luns) {
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(true);
        vdsReturnValue.setReturnValue(luns);
        return vdsReturnValue;
    }

    private Object createQueryReturnValueWith(Object value) {
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(value);
        return returnValue;
    }

    protected void prepareCommand() {
        parameters.setStoragePoolId(HE_SP_ID);
        parameters.setVdsId(HE_VDS_ID);
        parameters.setStorageDomainId(HE_SD_ID);
        // vds
        doReturn(vdsDao).when(cmd).getVdsDao();
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setStoragePoolId(HE_SP_ID);
        when(vdsDao.get(any(Guid.class))).thenReturn(vds);
        // base disk dao
        doReturn(dbFacade).when(cmd).getDbFacade();
        when(dbFacade.getBaseDiskDao()).thenReturn(baseDiskDao);
        List<BaseDisk> baseDisks = Arrays.asList(new BaseDisk());
        when(baseDiskDao.getDisksByAlias(anyString())).thenReturn(baseDisks);
        // remove disk
        when(backend.runInternalAction(
                eq(VdcActionType.RemoveDisk),
                any(VdsActionParameters.class))).thenReturn(successfulReturnValue());
        // Data center
        StoragePool pool = new StoragePool();
        pool.setStatus(StoragePoolStatus.Up);
        pool.setId(HE_SP_ID);
        when(storagePoolDao.get(HE_SP_ID)).thenReturn(pool);
        // compensation
        CompensationContext compensationContext = mock(CompensationContext.class);
        when(cmd.getCompensationContext()).thenReturn(compensationContext);
        doNothing().when(compensationContext).snapshotEntity(any(BusinessEntity.class));
        doNothing().when(compensationContext).stateChanged();
        when(cmd.getContext()).thenReturn(new CommandContext(new EngineContext()));
    }

    private VdcReturnValueBase successfulReturnValue() {
        VdcReturnValueBase value = new VdcReturnValueBase();
        value.setSucceeded(true);
        return value;
    }

    private void mockCommandCall(VdcActionType actionType, boolean withContext) {
        if (withContext) {
            doReturn(successfulReturnValue())
                    .when(backend).runInternalAction(eq(actionType),
                    any(VdcActionParametersBase.class),
                    any(CommandContext.class));
        } else {
            doReturn(successfulReturnValue())
                    .when(backend).runInternalAction(eq(actionType),
                    any(VdcActionParametersBase.class));
        }

    }

    protected StorageDomain mockGetExistingDomain(boolean answerWithDomain) {
        StorageDomain sd = null;
        List<StorageDomain> domains = Collections.emptyList();
        if (answerWithDomain) {
            sd = new StorageDomain();
            sd.getStorageStaticData().setConnection(new StorageServerConnections());
            domains = Arrays.asList(sd);
        }

        doReturn(createQueryReturnValueWith(domains))
                .when(backend).runInternalQuery(
                eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));

        return sd;
    }
}
