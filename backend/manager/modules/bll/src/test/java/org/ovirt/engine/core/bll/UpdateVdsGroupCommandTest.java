package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({DbFacade.class, CpuFlagsManagerHandler.class, Config.class, CpuFlagsManagerHandler.class,
        Backend.class, VersionSupport.class})
@RunWith(PowerMockRunner.class)
public class UpdateVdsGroupCommandTest {

    private static final Version VERSION_1_0 = new Version(1, 0);
    private static final Version VERSION_1_1 = new Version(1, 1);
    private static final Version VERSION_1_2 = new Version(1, 2);
    private static final Guid STORAGE_POOL_ID = Guid.NewGuid();

    @Mock private DbFacade dbFacade;
    @Mock private VdsGroupDAO vdsGroupDAO;
    @Mock private VdsStaticDAO vdsStaticDAO;
    @Mock private BackendInternal backendInternal;
    @Mock private StoragePoolDAO storagePoolDAO;

    private UpdateVdsGroupCommand<VdsGroupOperationParameters> cmd;


    @Before
    public void setUp() {
        initMocks(this);

        mockStatic(DbFacade.class);
        mockStatic(CpuFlagsManagerHandler.class);
        mockStatic(Config.class);
        mockStatic(CpuFlagsManagerHandler.class);
        mockStatic(Backend.class);

        when(DbFacade.getInstance()).thenReturn(dbFacade);
        when(dbFacade.getVdsGroupDAO()).thenReturn(vdsGroupDAO);
        when(dbFacade.getVdsStaticDAO()).thenReturn(vdsStaticDAO);
        when(dbFacade.getStoragePoolDAO()).thenReturn(storagePoolDAO);
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createDefaultVdsGroup());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createDefaultVdsGroup());
        when(Backend.getInstance()).thenReturn(backendInternal);

        VdsGroupOperationParameters params = new VdsGroupOperationParameters(createNewVdsGroup());
        cmd = new UpdateVdsGroupCommand<VdsGroupOperationParameters>(params);

    }

    @Test
    public void nameInUse() {
        createSimpleCommand();
        createCommandWithDifferentName();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_DO_ACTION_NAME_IN_USE);
    }

    @Test
    public void invalidVdsGroup() {
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(null);
        canDoActionFailedWithReason(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
    }

    @Test
    public void invalidCpuSelection() {
      canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_CPU_NOT_FOUND);
    }

    @Test
    public void illegalCpuChange() {
        createSimpleCommand();
        cpuExists();
        cpuManufacturersDontMatch();
        vdsGroupHasVds();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_ILLEGAL);
    }

    @Test
    public void invalidVersion() {
        createCommandWithInvalidVersion();
        cpuExists();
        cpuManufacturersMatch();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_GIVEN_VERSION_NOT_SUPPORTED);
    }

    @Test
    public void versionDecrease() {
        createCommandWithOlderVersion();
        cpuExists();
        cpuManufacturersMatch();
        canDoActionFailedWithReason(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
    }

    @Test
    public void updateWithLowerVersionThanHosts() {
        createSimpleCommand();
        cpuExists();
        cpuManufacturersMatch();
        VdsExistWithHigherVersion();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_LOWER_HOSTS);
    }

    @Test
    public void updateWithCpuLowerThanHost() {
        createSimpleCommand();
        cpuExists();
        cpuManufacturersMatch();
        clusterVersionIsSupported();
        clusterHasVds();
        CpuFlagsMissing();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_WITH_LOWER_HOSTS);
    }

    @Test
    public void updateStoragePool() {
        createCommandWithDifferentPool();
        cpuExists();
        cpuManufacturersMatch();
        clusterHasVds();
        cpuFlagsNotMissing();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_CHANGE_STORAGE_POOL);
    }

    @Test
    public void clusterAlreadyInLocalFs() {
        createCommandWithDefaultVdsGroup();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        cpuExists();
        cpuManufacturersMatch();
        clusterVersionIsSupported();
        allQueriesEmpty();
        storagePoolAlreadyHasCluster();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
    }

    @Test
    public void defaultClusterInLocalFs() {
        createCommandWithDefaultVdsGroup();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        cpuExists();
        cpuManufacturersMatch();
        clusterVersionIsSupported();
        allQueriesEmpty();
        canDoActionFailedWithReason(VdcBllMessages.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS);
    }

    @Test
    public void selectionAlgoNotNone() {
        createCommandWithPowerSaveVdsGroup();
        oldGroupIsDetachedDefault();
        storagePoolIsLocalFS();
        cpuExists();
        cpuManufacturersMatch();
        clusterVersionIsSupported();
        allQueriesEmpty();
        canDoActionFailedWithReason(VdcBllMessages.VDS_GROUP_SELECTION_ALGORITHM_MUST_BE_SET_TO_NONE_ON_LOCAL_STORAGE);
    }

    @Test
    public void vdsGroupWithNoCpu() {
        createCommandWithNoCpuName();
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createVdsGroupWithNoCpuName());
        when(vdsGroupDAO.getByName(anyString())).thenReturn(createVdsGroupWithNoCpuName());
        allQueriesEmpty();
        assertTrue(cmd.canDoAction());
    }

    private void createSimpleCommand() {
        createCommand(createNewVdsGroup());
    }

    private void createCommandWithOlderVersion() {
        createCommand(createVdsGroupWithOlderVersion());

    }

    private void createCommandWithInvalidVersion() {
        createCommand(createVdsGroupWithBadVersion());
    }

    private void createCommandWithDifferentPool() {
        createCommand(createVdsGroupWithDifferentPool());
    }

    private void createCommandWithDefaultVdsGroup() {
        createCommand(createDefaultVdsGroup());
    }

    private void createCommandWithNoCpuName() {
        createCommand(createVdsGroupWithNoCpuName());
    }

    private void createCommandWithPowerSaveVdsGroup() {
        createCommand(createVdsGroupWithPowerSave());
    }

    private void createCommand(final VDSGroup group) {
        setValidCpuVersionMap();
        VdsGroupOperationParameters params = new VdsGroupOperationParameters(group);
        cmd = new UpdateVdsGroupCommand<VdsGroupOperationParameters>(params);
    }

    private void createCommandWithDifferentName() {
        createCommand(createVdsGroupWithDifferentName());
    }

    private VDSGroup createVdsGroupWithDifferentName() {
        VDSGroup group = new VDSGroup();
        group.setname("BadName");
        return group;
    }

    private VDSGroup createNewVdsGroup() {
        VDSGroup group = new VDSGroup();
        group.setcompatibility_version(VERSION_1_1);
        group.setname("Default");
        return group;
    }

    private VDSGroup createDefaultVdsGroup() {
        VDSGroup group = new VDSGroup();
        group.setname("Default");
        group.setID(VDSGroup.DEFAULT_VDS_GROUP_ID);
        group.setcpu_name("Intel Conroe");
        group.setcompatibility_version(VERSION_1_1);
        group.setstorage_pool_id(STORAGE_POOL_ID);
        return group;
    }

    private VDSGroup createVdsGroupWithNoCpuName() {
        VDSGroup group = new VDSGroup();
        group.setname("Default");
        group.setID(VDSGroup.DEFAULT_VDS_GROUP_ID);
        group.setcompatibility_version(VERSION_1_1);
        group.setstorage_pool_id(STORAGE_POOL_ID);
        return group;
    }

    private VDSGroup createDetachedDefaultVdsGroup() {
        VDSGroup group = createDefaultVdsGroup();
        group.setstorage_pool_id(null);
        return group;
    }

    private VDSGroup createVdsGroupWithOlderVersion() {
        VDSGroup group = createNewVdsGroup();
        group.setcompatibility_version(VERSION_1_0);
        return group;
    }

    private VDSGroup createVdsGroupWithBadVersion() {
        VDSGroup group = createNewVdsGroup();
        group.setcompatibility_version(new Version(5, 0));
        return group;
    }

    private VDSGroup createVdsGroupWithDifferentPool() {
        VDSGroup group = createNewVdsGroup();
        group.setstorage_pool_id(Guid.NewGuid());
        return group;
    }

    private VDSGroup createVdsGroupWithPowerSave() {
        VDSGroup group = createDefaultVdsGroup();
        group.setselection_algorithm(VdsSelectionAlgorithm.PowerSave);
        return group;
    }

    private storage_pool createStoragePoolLocalFS() {
        storage_pool pool = new storage_pool();
        pool.setstorage_pool_type(StorageType.LOCALFS);
        return pool;
    }

    private void storagePoolIsLocalFS() {
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(createStoragePoolLocalFS());
    }

    private void oldGroupIsDetachedDefault() {
        when(vdsGroupDAO.get(any(Guid.class))).thenReturn(createDetachedDefaultVdsGroup());
    }

    private void storagePoolAlreadyHasCluster() {
        VDSGroup group = new VDSGroup();
        List<VDSGroup> groupList = new ArrayList<VDSGroup>();
        groupList.add(group);
        when(vdsGroupDAO.getAllForStoragePool(any(Guid.class))).thenReturn(groupList);
    }

    private void VdsExistWithHigherVersion() {
        VDS vds = new VDS();
        List<VDS> vdsList = new ArrayList<VDS>();
        vdsList.add(vds);
        VdcQueryReturnValue returnValue = mock(VdcQueryReturnValue.class);
        when(backendInternal.runInternalQuery(any(VdcQueryType.class), argThat(
                new ArgumentMatcher<VdcQueryParametersBase>() {
            @Override
            public boolean matches(final Object o) {
                SearchParameters param = (SearchParameters) o;
                return param.getSearchTypeValue().equals(SearchType.VDS);
            }
        })))
                .thenReturn(returnValue);
        when(returnValue.getReturnValue()).thenReturn(vdsList);
    }

    private void allQueriesEmpty() {
        VdcQueryReturnValue returnValue = mock(VdcQueryReturnValue.class);
        when(backendInternal.runInternalQuery(any(VdcQueryType.class), any(SearchParameters.class)))
                .thenReturn(returnValue);
        when(returnValue.getReturnValue()).thenReturn(Collections.emptyList());
    }

    private void vdsGroupHasVds() {
        List<VdsStatic> vdsList = new ArrayList<VdsStatic>();
        vdsList.add(new VdsStatic());
        when(vdsStaticDAO.getAllForVdsGroup(any(Guid.class))).thenReturn(vdsList);
    }

    private void clusterHasVds() {
        VDS vds = new VDS();
        vds.setsupported_cluster_levels(VERSION_1_1.toString());
        List<VDS> vdsList = new ArrayList<VDS>();
        vdsList.add(vds);
        VdcQueryReturnValue returnValue = mock(VdcQueryReturnValue.class);
        when(backendInternal.runInternalQuery(any(VdcQueryType.class), any(SearchParameters.class)))
                .thenReturn(returnValue);
        when(returnValue.getReturnValue()).thenReturn(vdsList);
    }

    private void CpuFlagsMissing() {
        List<String> strings = new ArrayList<String>();
        strings.add("foo");
        when(CpuFlagsManagerHandler.missingServerCpuFlags(anyString(), anyString(), any(Version.class)))
                .thenReturn(strings);
    }

    private void cpuFlagsNotMissing() {
        when(CpuFlagsManagerHandler.missingServerCpuFlags(anyString(), anyString(), any(Version.class)))
                .thenReturn(null);
    }

    private void clusterVersionIsSupported() {
        mockStatic(VersionSupport.class);
        when(VersionSupport.checkVersionSupported(any(Version.class))).thenReturn(true);
        when(VersionSupport.checkClusterVersionSupported(any(Version.class), any(VDS.class))).thenReturn(true);
    }

    private void cpuManufacturersDontMatch() {
        when(CpuFlagsManagerHandler.CheckIfCpusSameManufacture(anyString(), anyString(), any(Version.class)))
                .thenReturn(false);
    }

    private void cpuManufacturersMatch() {
        when(CpuFlagsManagerHandler.CheckIfCpusSameManufacture(anyString(), anyString(), any(Version.class)))
                .thenReturn(true);
    }

    private void cpuExists() {
        when(CpuFlagsManagerHandler.CheckIfCpusExist(anyString(), any(Version.class))).thenReturn(true);
    }

    private void setValidCpuVersionMap() {
        CpuFlagsManagerHandler.InitDictionaries();
        when(Config.GetValue(ConfigValues.SupportedClusterLevels)).thenReturn(createVersionSet());
    }

    private Set<Version> createVersionSet() {
        Set<Version> versions = new HashSet<Version>();
        versions.add(VERSION_1_0);
        versions.add(VERSION_1_1);
        versions.add(VERSION_1_2);
        return versions;
    }

    private void canDoActionFailedWithReason(final VdcBllMessages message) {
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue().getCanDoActionMessages().contains(message.toString()));
    }
}
