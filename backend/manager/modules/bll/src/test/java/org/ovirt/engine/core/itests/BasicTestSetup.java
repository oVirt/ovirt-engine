package org.ovirt.engine.core.itests;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.itests.AbstractBackendTest.testSequence;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.AddVdsCommand;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CpuFlagsManagerHandler;
import org.ovirt.engine.core.bll.VdsInstallHelper;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.GetVdsGroupByVdsGroupIdParameters;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This class will create basic virtualization "lab" composed from all basic entities, Host, Cluster, Storage,
 * DataCenter and VMs. Also a new session and a user will be created to simulate a running session. Combined with JUnit
 * test classes that extends {@link AbstractBackendTest} one can fire commands and queries or test directly the
 * {@link DbFacade} Bare in mind the this is a mock. Test scenarios running against this so-called lab are limited to
 * (and design to test) provisioning mainly and have no value (yet...) for physical actions like runVm etc. TODO
 * consider the the setup to work with predefined values (so it can represent *real* servers) or connect to a mock DB
 *
 */
public class BasicTestSetup {

    private static final DbFacade DB_FACADE = DbFacade.getInstance();
    private final VdcUser user;
    private VM vm;
    private VmTemplate template;
    private VDS host;
    private VDSGroup cluster;
    private storage_pool dataCenter;
    private storage_domains storage;
    private final Version latestSupportedCompatibilityVersion;
    private final String cpuForCompatibilityVersion;
    private final BackendInternal backend;

    @Mock
    private VdsInstallHelper vdsInstallHelper;

    public BasicTestSetup(BackendInternal backend) {
        MockitoAnnotations.initMocks(this);
        this.user = getUserBySessionId(AbstractBackendTest.getSessionId());
        this.backend = backend;
        latestSupportedCompatibilityVersion = findLatestSupportedClusterVersion();
        cpuForCompatibilityVersion =
                CpuFlagsManagerHandler.AllServerCpuList(latestSupportedCompatibilityVersion).get(0).getCpuName();
        createDataCenter();
        createCluster();
        createHost();
        createStorage();
        createVm();
    }

    /**
     * Get from configuration all supported cluster versions and return the latest.
     */
    private Version findLatestSupportedClusterVersion() {
        HashSet<Version> supportedVersions = Config.<HashSet<Version>> GetValue(
                ConfigValues.SupportedClusterLevels);
        Iterator<Version> vi = supportedVersions.iterator();
        Version result = vi.next();
        while (vi.hasNext()) {
            Version next = vi.next();
            if (next.compareTo(result) > 0) {
                result = next;
            }
        }
        return result;
    }

    /**
     * @param sessionId
     * @return The user that was stored on the session - automatically been set by {@link #generateUserSession()}
     */
    public VdcUser getUserBySessionId(String sessionId) {
        VdcUser user = (VdcUser) SessionDataContainer.getInstance().getUser(sessionId);
        return user;
    }

    public VdcUser getUser() {
        return user;
    }

    private void createVm() {
        VmStatic vm = new VmStatic();

        Date now = new Date(System.currentTimeMillis());
        vm.setvds_group_id(cluster.getId());
        vm.setId(new Guid(""));
        vm.setmem_size_mb(256);
        vm.setvm_type(VmType.Desktop);
        vm.setvmt_guid(Guid.Empty);
        vm.setdedicated_vm_for_vds(null);
        vm.setvm_type(VmType.Desktop);
        vm.setos(VmOsType.RHEL5);
        vm.setvm_name("vm" + testSequence);
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);
        vm.setcreation_date(now);

        ArrayList<DiskImageBase> diskInfoList = new ArrayList<DiskImageBase>();

        VmManagementParametersBase addVmFromScratchParams = new AddVmFromScratchParameters(vm, diskInfoList,
                getStorage().getId());

        //
        // diskInfoList.add(disk);
        addVmFromScratchParams.setDontCheckTemplateImages(true);
        VdcReturnValueBase addVmAction = backend.runInternalAction(VdcActionType.AddVmFromScratch,
                addVmFromScratchParams);
        Assert.assertTrue(addVmAction.getSucceeded());

        this.vm = DB_FACADE.getVmDAO().get(vm.getId());
        Assert.assertNotNull(this.vm);

        createVmDiskImage(vm, now);

    }

    private void createVmDiskImage(VmStatic vm, Date now) {
        Guid vmId = vm.getId();
        Guid ImageId = Guid.NewGuid();
        DiskImageBase disk =
                new DiskImage(false,
                        now,
                        now,
                        100,
                        Long.toString(AbstractBackendTest.testSequenceNumber),
                        ImageId,
                        "",
                        Guid.NewGuid(),
                        100,
                        vmId,
                        vmId,
                        ImageStatus.OK,
                        now,
                        "",
                        VmEntityType.VM, null,null);
        disk.setvolume_type(VolumeType.Sparse);
        disk.setvolume_format(VolumeFormat.COW);
        disk.setdisk_type(DiskType.Data);
        disk.setsize(100);
        disk.setinternal_drive_mapping(Long.toString(AbstractBackendTest.testSequenceNumber));
        disk.setdisk_interface(DiskInterface.IDE);
        disk.setboot(false);
        disk.setwipe_after_delete(false);
        disk.setpropagate_errors(PropagateErrors.Off);

        DiskImage image = new DiskImage(disk);
        image.setId(ImageId);
        image.setimageStatus(ImageStatus.OK);
        image.setvm_guid(vmId);
        image.setcreation_date(now);

        AddImageFromScratchParameters addImageParams = new AddImageFromScratchParameters(ImageId, vmId, disk);
        addImageParams.setStorageDomainId(storage.getId());
        addImageParams.setVmSnapshotId(Guid.NewGuid());
        addImageParams.setParentCommand(VdcActionType.AddDiskToVm);
        addImageParams.setEntityId(ImageId);
        addImageParams.setDestinationImageId(ImageId);
        AddImageFromScratchParameters parameters = addImageParams;
        VdcReturnValueBase addImageAction =
                Backend.getInstance().runInternalAction(VdcActionType.AddImageFromScratch, parameters);
        Assert.assertTrue(addImageAction.getSucceeded());
    }

    private void createStorage() {
        storage_server_connections connection = new storage_server_connections();
        connection.setconnection("1.1.1.1/common");
        connection.setstorage_type(StorageType.NFS);
        StorageServerConnectionParametersBase addStorgeConnectionParams = new StorageServerConnectionParametersBase(
                connection, host.getId());
        addStorgeConnectionParams.setStoragePoolId(dataCenter.getId());

        VdcReturnValueBase runInternalAction = backend.runInternalAction(VdcActionType.AddStorageServerConnection,
                addStorgeConnectionParams);
        Assert.assertTrue(runInternalAction.getSucceeded());

        storage_domain_static storageDomainStatic = new storage_domain_static();
        storageDomainStatic.setConnection(connection);
        storageDomainStatic.setstorage_domain_type(StorageDomainType.Data);
        storageDomainStatic.setstorage_type(StorageType.NFS);
        storageDomainStatic.setstorage_name(testSequence + "storage");
        storageDomainStatic.setstorage_pool_name(dataCenter.getname());

        Guid storageDomainId = Guid.NewGuid();
        storageDomainStatic.setId(storageDomainId);

        storage_domains storageDomain = new storage_domains(storageDomainId, connection.getconnection(), "storage"
                + testSequence, dataCenter.getId(), 2, 1, StorageDomainStatus.Active, dataCenter.getname(), dataCenter
                .getstorage_pool_type().getValue(), StorageType.NFS.getValue());
        storageDomain.setstatus(StorageDomainStatus.Active);
        storageDomain.setstorage_domain_type(StorageDomainType.Data);
        storage_domain_dynamic dynamicStorageDomain = new storage_domain_dynamic(null, storageDomainId, null);
        dynamicStorageDomain.setavailable_disk_size(50000);
        dynamicStorageDomain.setused_disk_size(10);
        storageDomain.setStorageDynamicData(dynamicStorageDomain);

        DB_FACADE.getStorageDomainStaticDAO().save(storageDomain.getStorageStaticData());
        DB_FACADE.getStorageDomainDynamicDAO().save(dynamicStorageDomain);
        DB_FACADE.getStorageDomainDynamicDAO().update(storageDomain.getStorageDynamicData());
        VdcReturnValueBase attachAction = backend.runInternalAction(VdcActionType.AttachStorageDomainToPool,
                new StorageDomainPoolParametersBase(storageDomainId, dataCenter.getId()));
        Assert.assertTrue(attachAction.getSucceeded());

        storage_pool_iso_map isoMap = DB_FACADE.getStoragePoolIsoMapDAO().get(new StoragePoolIsoMapId(
                storageDomainId, dataCenter.getId()));
        isoMap.setstatus(StorageDomainStatus.Active);
        DB_FACADE.getStoragePoolIsoMapDAO().updateStatus(isoMap.getId(), isoMap.getstatus());

        storage = storageDomain;
    }

    private void createHost() {
        Guid hostId = Guid.NewGuid();
        String hostName = "host" + testSequence;

        VdsStatic vdsStatic = new VdsStatic(hostName, "1.1.1.1", hostName, 22, cluster.getId(), hostId, hostName,
                false, VDSType.VDS);
        AddVdsActionParameters addHostParams = new AddVdsActionParameters(vdsStatic, "root");
        // Hack certificate path check
        Config.getConfigUtils().SetStringValue(ConfigValues.UseSecureConnectionWithServers.toString(), "false");

        Boolean isMLA = Config.<Boolean> GetValue(ConfigValues.IsMultilevelAdministrationOn);
        setIsMultiLevelAdministrationOn(Boolean.FALSE);
        mockVdsInstallerHelper();
        AddVdsCommand<AddVdsActionParameters> addVdsCommand = createAddVdsCommand(addHostParams);
        VdcReturnValueBase addHostAction = addVdsCommand.ExecuteAction();
        setIsMultiLevelAdministrationOn(isMLA);
        Assert.assertTrue(addHostAction.getSucceeded());

        hostId = (Guid) addHostAction.getActionReturnValue();
        host = (VDS) backend.runInternalQuery(VdcQueryType.GetVdsByVdsId, new GetVdsByVdsIdParameters(hostId))
                .getReturnValue();
        Assert.assertNotNull(host);
        VDS vds = DB_FACADE.getVdsDAO().get(hostId);
        vds.setstatus(VDSStatus.Up);
        DB_FACADE.getVdsDynamicDAO().update(vds.getDynamicData());
    }

    private AddVdsCommand<AddVdsActionParameters> createAddVdsCommand(AddVdsActionParameters addHostParams) {
        AddVdsCommand<AddVdsActionParameters> spyVdsCommand =
                spy(new AddVdsCommand<AddVdsActionParameters>(addHostParams));
        when(spyVdsCommand.getVdsInstallHelper()).thenReturn(vdsInstallHelper);
        return spyVdsCommand;
    }

    private void mockVdsInstallerHelper() {
        when(vdsInstallHelper.connectToServer(anyString(), anyString(), anyLong())).thenReturn(true);
        when(vdsInstallHelper.getServerUniqueId()).thenReturn("");
    }

    private void setIsMultiLevelAdministrationOn(Boolean isMLA) {
        Config.getConfigUtils().SetStringValue(ConfigValues.IsMultilevelAdministrationOn.toString(), isMLA.toString());
    }

    private void createCluster() {
        VDSGroup group = new VDSGroup();
        group.setname("cluster" + testSequence);
        group.setstorage_pool_id(dataCenter.getId());
        group.setcpu_name(cpuForCompatibilityVersion);
        group.setcompatibility_version(latestSupportedCompatibilityVersion);

        Guid clusterId = (Guid) backend.runInternalAction(VdcActionType.AddVdsGroup,
                new VdsGroupOperationParameters(group)).getActionReturnValue();
        cluster = (VDSGroup) backend.runInternalQuery(VdcQueryType.GetVdsGroupByVdsGroupId,
                new GetVdsGroupByVdsGroupIdParameters(clusterId)).getReturnValue();
        Assert.assertNotNull(cluster);
    }

    private void createDataCenter() {
        storage_pool storagePool = new storage_pool();
        storagePool.setcompatibility_version(latestSupportedCompatibilityVersion);
        storagePool.setname("DataCenter" + testSequence);
        storagePool.setstorage_pool_type(StorageType.NFS);
        storagePool.setstatus(StoragePoolStatus.Up);

        Guid dataCenterId = (Guid) backend.runInternalAction(VdcActionType.AddEmptyStoragePool,
                new StoragePoolManagementParameter(storagePool)).getActionReturnValue();
        dataCenter = (storage_pool) backend.runInternalQuery(VdcQueryType.GetStoragePoolById,
                new StoragePoolQueryParametersBase(dataCenterId)).getReturnValue();
        Assert.assertNotNull(dataCenter);
    }

    public VM getVm() {
        return vm;
    }

    public VmTemplate getTemplate() {
        return template;
    }

    public VDS getHost() {
        return host;
    }

    public VDSGroup getCluster() {
        return cluster;
    }

    public storage_pool getDataCenter() {
        return dataCenter;
    }

    public storage_domains getStorage() {
        return storage;
    }

    public void cleanSetup() {
        removeVM();
        removeStorage();
        removeHost();
        removeCluster();
        removeDatacenter();
        removeUser();
    }

    private void removeUser() {
        Guid userId = getUser().getUserId();
        List<permissions> perms = DB_FACADE.getPermissionDAO().getAllForAdElement(userId);
        for (permissions p : perms) {
            DB_FACADE.getPermissionDAO().remove(p.getId());
        }
        DB_FACADE.getDbUserDAO().remove(userId);
        System.out.println("-- removed user " + getUser().getUserName() + " and its permissions -- ");
    }

    private void removeDatacenter() {
        DB_FACADE.getStoragePoolDAO().remove(dataCenter.getId());
        System.out.println("-- removed Data Center " + dataCenter.getname() + "-- ");
    }

    private void removeCluster() {
        // DbFacade.getInstance().RemoveVDSGroups(cluster.getID());
        backend.RunAction(VdcActionType.RemoveVdsGroup, new VdsGroupParametersBase(cluster.getId()));
        System.out.println("-- removed cluster " + cluster.getname() + " -- ");
    }

    private void removeHost() {
        DB_FACADE.getVdsDynamicDAO().remove(host.getId());
        DB_FACADE.getVdsStatisticsDAO().remove(host.getId());
        DB_FACADE.getVdsStaticDAO().remove(host.getId());
        System.out.println("-- removed Host " + host.gethost_name() + " -- ");
    }

    private void removeStorage() {

        Guid id = storage.getId();
        DB_FACADE.getStorageDomainDynamicDAO().remove(id);
        List<DiskImage> snapshots = DB_FACADE.getDiskImageDAO().getAllSnapshotsForStorageDomain(id);
        for (DiskImage i : snapshots) {
            DB_FACADE.getDiskImageDAO().remove(i.getId());
        }
        DB_FACADE.getStorageDomainStaticDAO().remove(id);
        System.out.println("-- removed storage " + storage.getstorage_name() + " and its snapshots -- ");
    }

    private void removeVM() {
        DB_FACADE.getDiskImageDAO().removeAllForVmId(vm.getId());
        DB_FACADE.getVmDAO().remove(vm.getId());
        System.out.println("-- removed VM " + vm.getvm_name() + " and its images -- ");

    }
}
