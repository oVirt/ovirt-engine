package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.storage.domain.ImportHostedEngineStorageDomainCommand.SUPPORTED_DOMAIN_TYPES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
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
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ImportHostedEngineStorageDomainCommandTest extends BaseCommandTest {

    private static final Guid HE_SP_ID = Guid.createGuidFromString("35000000-0000-0000-0000-000000000000");
    private static final Guid HE_SD_ID = Guid.createGuidFromString("35100000-0000-0000-0000-000000000000");
    private static final Guid HE_VDS_ID = Guid.createGuidFromString("35200000-0000-0000-0000-000000000000");
    private static final Guid VG_ID = Guid.createGuidFromString("35300000-0000-0000-0000-000000000000");
    private static final String ISCSIUSER = "iscsiuser";
    private static final String ISCSIPASS = "iscsipass";

    @Mock
    private HostedEngineHelper hostedEngineHelper;
    private StorageDomainManagementParameter parameters = new StorageDomainManagementParameter();
    @InjectMocks
    @Spy
    private ImportHostedEngineStorageDomainCommand<StorageDomainManagementParameter> cmd =
            new ImportHostedEngineStorageDomainCommand<>(
                parameters, CommandContext.createContext(parameters.getSessionId()));
    @Mock
    private BackendInternal backend;
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

    @BeforeEach
    public void initTest() {
        prepareCommand();
    }

    @Test
    public void failIfDcNotActive() {
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
    public void failsIfImported() {
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
    public void failsInNotImportedAndNotExists() {
        mockGetExistingDomain(false);

        cmd.init();
        ValidateTestUtils.runAndAssertValidateFailure(
                "",
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        verify(backend, times(1)).runInternalQuery(
                eq(QueryType.GetExistingStorageDomainList),
                any());
        verify(cmd, times(0)).executeCommand();
    }

    @Test
    public void storageTypeUnsupported() {
        StorageDomain sd = mockGetExistingDomain(true);
        sd.setStorageType(StorageType.CINDER);
        sd.setId(HE_SD_ID);

        cmd.init();
        ValidateTestUtils.runAndAssertValidateFailure(
                "",
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED);
        verify(backend, times(1)).runInternalQuery(
                eq(QueryType.GetExistingStorageDomainList),
                any());
        verify(cmd, times(0)).executeCommand();
    }

    @Test
    public void validatePass() {
        StorageDomain sd = mockGetExistingDomain(true);
        int i = new Random().nextInt(SUPPORTED_DOMAIN_TYPES.length);
        sd.setStorageType(SUPPORTED_DOMAIN_TYPES[i]);
        sd.setId(HE_SD_ID);

        cmd.init();
        assertTrue(cmd.validate());
    }

    @Test
    public void callConcreteAddSD() {
        StorageDomain sd = mockGetExistingDomain(true);
        sd.setStorageType(StorageType.NFS);
        sd.setId(HE_SD_ID);
        mockCommandCall(ActionType.AddExistingFileStorageDomain, true);
        mockCommandCall(ActionType.AttachStorageDomainToPool, true);

        cmd.init();
        cmd.validate();
        cmd.executeCommand();

        verify(storageServerConnectionDao, times(1))
            .save(sd.getStorageStaticData().getConnection());
        verify(backend, times(1)).runInternalAction(
                eq(ActionType.AddExistingFileStorageDomain),
                any(),
                any());
    }

    @Test
    public void callConcreteAddBlockSD() {
        StorageDomain sd = mockGetExistingDomain(true);
        sd.setId(HE_SD_ID);
        sd.setStorageType(StorageType.ISCSI);
        sd.setStorage(VG_ID.toString());
        mockCommandCall(ActionType.RemoveDisk, false);
        mockCommandCall(ActionType.AddExistingBlockStorageDomain, true);
        doReturn(createVdsReturnValue(createSdLuns()))
                .when(vdsBroker).runVdsCommand(eq(VDSCommandType.GetDeviceList), any());
        mockCommandCall(ActionType.AttachStorageDomainToPool, true);

        cmd.init();
        cmd.validate();
        cmd.executeCommand();

        verify(backend, times(1)).runInternalAction(
                eq(ActionType.RemoveDisk),
                any());
        verify(backend, times(1)).runInternalAction(
                eq(ActionType.AddExistingBlockStorageDomain),
                any(),
                any());

        assertTrue(cmd.getReturnValue().getSucceeded());
        assertEquals(cmd.getReturnValue().getActionReturnValue(), sd);
        assertEquals(ISCSIUSER, sd.getStorageStaticData().getConnection().getUserName());
        assertEquals(ISCSIPASS, sd.getStorageStaticData().getConnection().getPassword());
        assertEquals(sd.getStorageStaticData().getStorage(), VG_ID.toString());
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
        QueryReturnValue returnValue = new QueryReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(value);
        return returnValue;
    }

    protected void prepareCommand() {
        parameters.setStoragePoolId(HE_SP_ID);
        parameters.setVdsId(HE_VDS_ID);
        parameters.setStorageDomainId(HE_SD_ID);
        // vds
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setStoragePoolId(HE_SP_ID);
        when(vdsDao.get(any())).thenReturn(vds);
        List<BaseDisk> baseDisks = Collections.singletonList(new BaseDisk());
        when(baseDiskDao.getDisksByAlias(any())).thenReturn(baseDisks);
        // Data center
        StoragePool pool = new StoragePool();
        pool.setStatus(StoragePoolStatus.Up);
        pool.setId(HE_SP_ID);
        when(storagePoolDao.get(HE_SP_ID)).thenReturn(pool);
        // compensation
        CompensationContext compensationContext = mock(CompensationContext.class);
        when(cmd.getCompensationContext()).thenReturn(compensationContext);
        when(cmd.getContext()).thenReturn(new CommandContext(new EngineContext()));
    }

    private ActionReturnValue successfulReturnValue() {
        ActionReturnValue value = new ActionReturnValue();
        value.setSucceeded(true);
        return value;
    }

    private void mockCommandCall(ActionType actionType, boolean withContext) {
        if (withContext) {
            doReturn(successfulReturnValue())
                    .when(backend).runInternalAction(eq(actionType),
                    any(),
                    any());
        } else {
            doReturn(successfulReturnValue())
                    .when(backend).runInternalAction(eq(actionType),
                    any());
        }

    }

    protected StorageDomain mockGetExistingDomain(boolean answerWithDomain) {
        StorageDomain sd = null;
        List<StorageDomain> domains = Collections.emptyList();
        if (answerWithDomain) {
            sd = new StorageDomain();
            sd.getStorageStaticData().setConnection(new StorageServerConnections());
            domains = Collections.singletonList(sd);
        }

        doReturn(createQueryReturnValueWith(domains))
                .when(backend).runInternalQuery(
                eq(QueryType.GetExistingStorageDomainList),
                any());

        return sd;
    }
}
