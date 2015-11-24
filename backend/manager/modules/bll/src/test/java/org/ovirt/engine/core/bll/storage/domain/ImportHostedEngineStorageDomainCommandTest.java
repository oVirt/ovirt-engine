package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.storage.domain.ImportHostedEngineStorageDomainCommand.SUPPORTED_DOMAIN_TYPES;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class ImportHostedEngineStorageDomainCommandTest {

    private static final Guid HE_SP_ID = Guid.createGuidFromString("35000000-0000-0000-0000-000000000000");
    private static final Guid HE_SD_ID = Guid.createGuidFromString("35100000-0000-0000-0000-000000000000");
    private static final Guid HE_VDS_ID = Guid.createGuidFromString("35200000-0000-0000-0000-000000000000");
    private static final Guid VG_ID = Guid.createGuidFromString("35300000-0000-0000-0000-000000000000");
    private static final String HOSTED_STORAGE_NAME = "hosted_storage";
    private static final String ISCSIUSER = "iscsiuser";
    private static final String ISCSIPASS = "iscsipass";

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
            mockConfig(ConfigValues.HostedEngineStorageDomainName, HOSTED_STORAGE_NAME));
    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();
    @Mock
    private HostedEngineHelper hostedEngineHelper;
    private StorageDomainManagementParameter parameters = new StorageDomainManagementParameter();
    @InjectMocks @Spy
    private ImportHostedEngineStorageDomainCommand cmd = new ImportHostedEngineStorageDomainCommand(parameters);
    @Mock
    private BackendInternal backend;
    @Mock
    private DbFacade dbFacade;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private BaseDiskDao baseDiskDao;

    @Before
    public void initTest() {
        prepareCommand();
    }

    @Test
    public void failsIfImported() throws Exception {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(new StorageDomainStatic());

        cmd.canDoAction();

        CanDoActionTestUtils.assertCanDoActionMessages(
                "",
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
        verify(backend, times(0)).runInternalQuery(
                eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));
        verify(cmd, times(0)).executeCommand();
    }

    @Test
    public void failsInNotImportedAndNotExists() throws Exception {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(null);
        doReturn(createQueryReturnValueWith(Collections.emptyList()))
                .when(backend).runInternalQuery(eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(
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
        StorageDomain sd = new StorageDomain();
        sd.setStorageType(StorageType.CINDER);
        sd.setStorageName(HOSTED_STORAGE_NAME);
        doReturn(createQueryReturnValueWith(Arrays.asList(sd)))
                .when(backend).runInternalQuery(eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(
                "",
                cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED);
        verify(backend, times(1)).runInternalQuery(
                eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));
        verify(cmd, times(0)).executeCommand();
    }

    @Test
    public void canDoPass() {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(null);
        StorageDomain sd = new StorageDomain();
        int i = new Random().nextInt(SUPPORTED_DOMAIN_TYPES.length);
        sd.setStorageType(SUPPORTED_DOMAIN_TYPES[i]);
        sd.setStorageName(HOSTED_STORAGE_NAME);
        doReturn(createQueryReturnValueWith(Arrays.asList(sd)))
                .when(backend).runInternalQuery(eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));

        assertTrue(cmd.canDoAction());
    }

    @Test
    public void callConcreteAddSD() {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(null);
        StorageDomain sd = new StorageDomain();
        sd.setStorageName(HOSTED_STORAGE_NAME);
        sd.setStorageType(StorageType.NFS);
        doReturn(createQueryReturnValueWith(Arrays.asList(sd)))
                .when(backend).runInternalQuery(eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));
        doReturn(successfulReturnValue())
                .when(backend).runInternalAction(eq(VdcActionType.AddExistingFileStorageDomain),
                any(VdcActionParametersBase.class));
        doReturn(successfulReturnValue())
                .when(backend).runInternalAction(eq(VdcActionType.AttachStorageDomainToPool),
                any(VdcActionParametersBase.class));

        cmd.canDoAction();
        cmd.executeCommand();

        verify(backend, times(1)).runInternalAction(
                eq(VdcActionType.AddExistingFileStorageDomain),
                any(VdcActionParametersBase.class));
    }

    @Test
    public void callConcreteAddBlockSD() {
        when(hostedEngineHelper.getStorageDomain()).thenReturn(null);
        StorageDomain sd = new StorageDomain();
        sd.setId(HE_SD_ID);
        sd.setStorageName(HOSTED_STORAGE_NAME);
        sd.setStorageType(StorageType.ISCSI);
        sd.setStorage(VG_ID.toString());
        doReturn(createQueryReturnValueWith(Arrays.asList(sd)))
                .when(backend).runInternalQuery(eq(VdcQueryType.GetExistingStorageDomainList),
                any(VdcQueryParametersBase.class));
        doReturn(successfulReturnValue())
                .when(backend).runInternalAction(eq(VdcActionType.AddExistingBlockStorageDomain),
                any(VdcActionParametersBase.class));
        doReturn(createVdsReturnValue(createSdLuns()))
                .when(cmd).runVdsCommand(eq(VDSCommandType.GetDeviceList), any(VDSParametersBase.class));
        doReturn(successfulReturnValue())
                .when(backend).runInternalAction(eq(VdcActionType.AttachStorageDomainToPool),
                any(VdcActionParametersBase.class));

        cmd.canDoAction();
        cmd.executeCommand();

        verify(backend, times(1)).runInternalAction(
                eq(VdcActionType.AddExistingBlockStorageDomain),
                any(VdcActionParametersBase.class));

        assertTrue(cmd.getSucceeded());
        assertEquals(cmd.getReturnValue().getActionReturnValue(), sd);
        assertTrue(sd.getStorageStaticData().getConnection().getuser_name().equals(ISCSIUSER));
        assertTrue(sd.getStorageStaticData().getConnection().getpassword().equals(ISCSIPASS));
        assertTrue(sd.getStorageStaticData().getStorage().equals(VG_ID.toString()));
    }

    protected ArrayList<LUNs> createSdLuns() {
        LUNs lun = new LUNs();
        lun.setvolume_group_id(VG_ID.toString());
        lun.setStorageDomainId(HE_SD_ID);
        ArrayList<StorageServerConnections> connections = new ArrayList<>();
        StorageServerConnections connection = new StorageServerConnections();
        connection.setuser_name(ISCSIUSER);
        connection.setpassword(ISCSIPASS);
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
        doReturn(backend).when(cmd).getBackend();
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
    }

    private VdcReturnValueBase successfulReturnValue() {
        VdcReturnValueBase value = new VdcReturnValueBase();
        value.setSucceeded(true);
        return value;
    }
}
