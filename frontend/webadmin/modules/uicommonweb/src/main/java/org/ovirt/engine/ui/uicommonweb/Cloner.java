package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.Version;

@SuppressWarnings("unused")
public final class Cloner
{
    public static Object clone(Object instance)
    {
        if (instance instanceof VM)
        {
            return cloneVM((VM) instance);
        }
        if (instance instanceof VDS)
        {
            return cloneVDS((VDS) instance);
        }
        if (instance instanceof VDSGroup)
        {
            return cloneVDSGroup((VDSGroup) instance);
        }
        if (instance instanceof StoragePool)
        {
            return cloneStorage_pool((StoragePool) instance);
        }
        if (instance instanceof Network)
        {
            return cloneNetwork((Network) instance);
        }
        if (instance instanceof NetworkCluster)
        {
            return cloneNetworkCluster((NetworkCluster) instance);
        }
        if (instance instanceof ProviderNetwork)
        {
            return cloneProviderNetwork((ProviderNetwork) instance);
        }
        if (instance instanceof VmPool)
        {
            return cloneVmPool((VmPool) instance);
        }
        if (instance instanceof StorageDomainStatic)
        {
            return cloneStorageDomainStatic((StorageDomainStatic) instance);
        }
        if (instance instanceof VmTemplate)
        {
            return cloneVmTemplate((VmTemplate) instance);
        }
        if (instance instanceof VmNetworkInterface)
        {
            return cloneVmNetworkInterface((VmNetworkInterface) instance);
        }
        if (instance instanceof VdsNetworkInterface)
        {
            return cloneVdsNetworkInterface((VdsNetworkInterface) instance);
        }
        if (instance instanceof VmStatic)
        {
            return cloneVmStatic((VmStatic) instance);
        }
        if (instance instanceof Version)
        {
            return cloneVersion((Version) instance);
        }
        if (instance instanceof ClusterPolicy)
        {
            return cloneClusterPolicy((ClusterPolicy) instance);
        }
        // Throw exception to determine development needs.
        throw new NotImplementedException();
    }

    private static Object cloneVM(VM instance)
    {
        if (instance == null)
        {
            return null;
        }

        VM vm = new VM();

        vm.setAcpiEnable(instance.getAcpiEnable());
        // TODO: this field is read only in serialization - not sure why it is cloned
        // vm.ActualDiskWithSnapshotsSize = instance.ActualDiskWithSnapshotsSize;
        vm.setAppList(instance.getAppList());
        vm.setAutoStartup(instance.isAutoStartup());
        vm.setBootSequence(instance.getBootSequence());
        vm.setClientIp(instance.getClientIp());
        vm.setCpuPerSocket(instance.getCpuPerSocket());
        vm.setCpuSys(instance.getCpuSys());
        vm.setCpuUser(instance.getCpuUser());
        vm.setDedicatedVmForVds(instance.getDedicatedVmForVds());
        vm.setDefaultBootSequence(instance.getDefaultBootSequence());
        vm.setDefaultDisplayType(instance.getDefaultDisplayType());
        // TODO: 1. DiskList is an array - CopyTo should be considered (if it can be converted to java, otherwise a
        // simple loop is needed)
        // TODO: 2. it is also read only in serialization, so not sure why it is cloned. it is manipulated via
        // addDriveToImageMap
        // vm.DiskList = instance.DiskList;
        vm.setDiskSize(instance.getDiskSize());
        // TODO: this is also an object, so needs to be cloned as well. while it is only accessed via VM.DiskMap, which
        // creates a dictionary
        // from it - actually the DiskImage's themselves are probably sharing the same reference...
        vm.setDisplay(instance.getDisplay());
        vm.setDisplayIp(instance.getDisplayIp());
        vm.setDisplaySecurePort(instance.getDisplaySecurePort());
        vm.setDisplayType(instance.getDisplayType());
        vm.getDynamicData().setVncKeyboardLayout(instance.getDynamicData().getVncKeyboardLayout());
        vm.setElapsedTime(instance.getElapsedTime());
        vm.setRoundedElapsedTime(instance.getRoundedElapsedTime());
        vm.setExitMessage(instance.getExitMessage());
        vm.setExitStatus(instance.getExitStatus());
        vm.setExitReason(instance.getExitReason());
        vm.setFailBack(instance.isFailBack());
        vm.setConsoleCurrentUserName(instance.getConsoleCurentUserName());
        vm.setGuestCurrentUserName(instance.getGuestCurentUserName());
        vm.setConsoleUserId(instance.getConsoleUserId());
        vm.setGuestLastLoginTime(instance.getGuestLastLoginTime());
        vm.setGuestLastLogoutTime(instance.getGuestLastLogoutTime());
        vm.setGuestOs(instance.getGuestOs());
        vm.setGuestRequestedMemory(instance.getGuestRequestedMemory());
        // TODO: Object, should be "cloned" (probably easiest via new Version(instance.GuestAgentVersion.ToString())
        // pay attention NOT to use lower case version in UICommon code.
        vm.setGuestAgentVersion(instance.getGuestAgentVersion());
        vm.setInitrdUrl(instance.getInitrdUrl());
        // TODO: array - need to consider cloning of array, and of actual interfaces
        vm.setInterfaces(instance.getInterfaces());
        vm.setInitialized(instance.isInitialized());
        vm.setStateless(instance.isStateless());
        vm.setRunAndPause(instance.isRunAndPause());
        vm.setIsoPath(instance.getIsoPath());
        vm.setKernelParams(instance.getKernelParams());
        vm.setKernelUrl(instance.getKernelUrl());
        vm.setKvmEnable(instance.getKvmEnable());
        // TODO: Guid is an object, but code should treat it as immutable, and not change it's uuid directly.
        // (quick skim of code shows this should be safe with current code)
        vm.setLastVdsRunOn(instance.getLastVdsRunOn());
        vm.setMigratingToVds(instance.getMigratingToVds());
        vm.setMigrationSupport(instance.getMigrationSupport());
        vm.setNiceLevel(instance.getNiceLevel());
        vm.setUseHostCpuFlags(instance.isUseHostCpuFlags());
        // TODO: this is readonly in java, since it is computed.
        // options: use calculation here in cloner, or still wrap this in VM instead of serializing it
        // vm.num_of_cpus = instance.num_of_cpus;
        vm.setNumOfMonitors(instance.getNumOfMonitors());
        vm.setAllowConsoleReconnect(instance.getAllowConsoleReconnect());
        vm.setNumOfSockets(instance.getNumOfSockets());
        vm.setOrigin(instance.getOrigin());
        vm.setVmPauseStatus(instance.getVmPauseStatus());
        vm.setPriority(instance.getPriority());
        vm.setRunOnVds(instance.getRunOnVds());
        vm.setRunOnVdsName(instance.getRunOnVdsName());
        vm.setSession(instance.getSession());
        // TODO: see version comment above
        vm.setSpiceDriverVersion(instance.getSpiceDriverVersion());
        vm.setStatus(instance.getStatus());
        vm.setStoragePoolId(instance.getStoragePoolId());
        vm.setStoragePoolName(instance.getStoragePoolName());
        vm.setTimeZone(instance.getTimeZone());
        vm.setTransparentHugePages(instance.isTransparentHugePages());
        vm.setUsageCpuPercent(instance.getUsageCpuPercent());
        vm.setUsageMemPercent(instance.getUsageMemPercent());
        vm.setUsageNetworkPercent(instance.getUsageNetworkPercent());
        vm.setUsbPolicy(instance.getUsbPolicy());
        vm.setUtcDiff(instance.getUtcDiff());
        vm.setVdsGroupCompatibilityVersion(instance.getVdsGroupCompatibilityVersion());
        vm.setVdsGroupId(instance.getVdsGroupId());
        vm.setVdsGroupName(instance.getVdsGroupName());
        vm.setVmCreationDate(instance.getVmCreationDate());
        vm.setVmDescription(instance.getVmDescription());
        vm.setComment(instance.getComment());
        vm.setId(instance.getId());
        vm.setVmHost(instance.getVmHost());
        vm.setVmIp(instance.getVmIp());
        vm.setVmFQDN(instance.getVmFQDN());
        vm.setLastStartTime(instance.getLastStartTime());
        vm.setVmMemSizeMb(instance.getVmMemSizeMb());
        vm.setName(instance.getName());
        vm.setVmOs(instance.getVmOsId());
        vm.setVmPid(instance.getVmPid());
        vm.setVmType(instance.getVmType());
        vm.setVmPoolId(instance.getVmPoolId());
        vm.setVmPoolName(instance.getVmPoolName());
        vm.setVmtGuid(instance.getVmtGuid());
        vm.setVmtName(instance.getVmtName());
        vm.setCreatedByUserId(instance.getCreatedByUserId());
        vm.setClusterArch(instance.getClusterArch());
        vm.setOriginalTemplateGuid(instance.getOriginalTemplateGuid());
        vm.setOriginalTemplateName(instance.getOriginalTemplateName());
        vm.setMigrationDowntime(instance.getMigrationDowntime());
        vm.setUseLatestVersion(instance.isUseLatestVersion());
        vm.setSerialNumberPolicy(instance.getSerialNumberPolicy());
        vm.setCustomSerialNumber(instance.getCustomSerialNumber());
        vm.setBootMenuEnabled(instance.isBootMenuEnabled());
        vm.setSpiceFileTransferEnabled(instance.isSpiceFileTransferEnabled());
        vm.setSpiceCopyPasteEnabled(instance.isSpiceCopyPasteEnabled());

        return vm;
    }

    private static Object cloneVersion(Version instance)
    {
        return new Version(instance.toString());
    }

    private static Object cloneVDS(VDS instance)
    {
        VDS obj = new VDS();

        obj.setHostName(instance.getHostName());
        obj.setSshKeyFingerprint(instance.getSshKeyFingerprint());
        obj.setSshPort(instance.getSshPort());
        obj.setSshUsername(instance.getSshUsername());
        obj.setManagementIp(instance.getManagementIp());
        obj.setPmUser(instance.getPmUser());
        obj.setPmPassword(instance.getPmPassword());
        obj.setPmType(instance.getPmType());
        obj.setPmOptionsMap(instance.getPmOptionsMap());

        obj.setPmSecondaryIp(instance.getManagementIp());
        obj.setPmSecondaryUser(instance.getPmUser());
        obj.setPmSecondaryPassword(instance.getPmPassword());
        obj.setPmSecondaryType(instance.getPmType());
        obj.setPmSecondaryOptionsMap(instance.getPmOptionsMap());

        obj.setpm_enabled(instance.getpm_enabled());
        obj.setPmSecondaryConcurrent(instance.isPmSecondaryConcurrent());
        obj.setDisablePowerManagementPolicy(instance.isDisablePowerManagementPolicy());
        obj.setPmKdumpDetection(instance.isPmKdumpDetection());

        obj.setPort(instance.getPort());
        obj.setServerSslEnabled(instance.isServerSslEnabled());
        obj.setVdsGroupId(instance.getVdsGroupId());
        obj.setId(instance.getId());
        obj.setVdsName(instance.getName());
        obj.setVdsStrength(instance.getVdsStrength());
        obj.setVdsType(instance.getVdsType());
        obj.setUniqueId(instance.getUniqueId());
        obj.setVdsSpmPriority(instance.getVdsSpmPriority());

        return obj;
    }

    private static StoragePool cloneStorage_pool(StoragePool instance)
    {
        StoragePool obj = new StoragePool();

        obj.setdescription(instance.getdescription());
        obj.setComment(instance.getComment());
        obj.setId(instance.getId());
        obj.setName(instance.getName());
        obj.setIsLocal(instance.isLocal());
        obj.setStatus(instance.getStatus());

        obj.setmaster_domain_version(instance.getmaster_domain_version());
        obj.setLVER(instance.getLVER());
        obj.setrecovery_mode(instance.getrecovery_mode());
        obj.setspm_vds_id(instance.getspm_vds_id());
        obj.setcompatibility_version(instance.getcompatibility_version());

        return obj;
    }

    private static VDSGroup cloneVDSGroup(VDSGroup instance)
    {
        VDSGroup obj = new VDSGroup();
        obj.setId(instance.getId());
        obj.setName(instance.getName());
        obj.setdescription(instance.getdescription());
        obj.setComment(instance.getComment());
        obj.setcpu_name(instance.getcpu_name());

        obj.setcompatibility_version(instance.getcompatibility_version());
        obj.setMigrateOnError(instance.getMigrateOnError());
        obj.setTransparentHugepages(instance.getTransparentHugepages());

        obj.setStoragePoolId(instance.getStoragePoolId());
        obj.setmax_vds_memory_over_commit(instance.getmax_vds_memory_over_commit());
        obj.setCountThreadsAsCores(instance.getCountThreadsAsCores());
        obj.setEmulatedMachine(instance.getEmulatedMachine());
        obj.setDetectEmulatedMachine(instance.isDetectEmulatedMachine());
        obj.setArchitecture(instance.getArchitecture());
        obj.setSerialNumberPolicy(instance.getSerialNumberPolicy());
        obj.setCustomSerialNumber(instance.getCustomSerialNumber());
        obj.setFencingPolicy(new FencingPolicy(instance.getFencingPolicy()));

        return obj;
    }

    private static Network cloneNetwork(Network instance)
    {
        Network obj = new Network();

        obj.setAddr(instance.getAddr());
        obj.setDescription(instance.getDescription());
        obj.setComment(instance.getComment());
        obj.setId(instance.getId());
        obj.setName(instance.getName());
        obj.setSubnet(instance.getSubnet());
        obj.setGateway(instance.getGateway());
        obj.setType(instance.getType());
        obj.setVlanId(instance.getVlanId());
        obj.setStp(instance.getStp());
        obj.setDataCenterId(instance.getDataCenterId());
        obj.setMtu(instance.getMtu());
        obj.setVmNetwork(instance.isVmNetwork());
        if (instance.getCluster() !=null){
            obj.setCluster(cloneNetworkCluster(instance.getCluster()));
        }
        if (instance.getProvidedBy() != null) {
            obj.setProvidedBy(cloneProviderNetwork(instance.getProvidedBy()));
        }
        return obj;
    }

    private static NetworkCluster cloneNetworkCluster(NetworkCluster instance)
    {
        NetworkCluster obj = new NetworkCluster();

        obj.setId(instance.getId());
        obj.setStatus(instance.getStatus());
        obj.setDisplay(instance.isDisplay());
        obj.setRequired(instance.isRequired());
        obj.setMigration(instance.isMigration());
        return obj;
    }

    private static ProviderNetwork cloneProviderNetwork(ProviderNetwork instance)
    {
        ProviderNetwork obj = new ProviderNetwork();

        obj.setExternalId(instance.getExternalId());
        obj.setProviderId(instance.getProviderId());
        return obj;
    }

    private static VmPool cloneVmPool(VmPool instance)
    {
        VmPool obj = new VmPool();

        obj.setVmPoolId(instance.getVmPoolId());
        obj.setName(instance.getName());
        obj.setVmPoolType(instance.getVmPoolType());
        obj.setVdsGroupId(instance.getVdsGroupId());

        obj.setVmPoolType(instance.getVmPoolType());
        obj.setParameters(instance.getParameters());
        obj.setDefaultEndTime(instance.getDefaultEndTime());
        obj.setDefaultStartTime(instance.getDefaultStartTime());
        obj.setDefaultTimeInDays(instance.getDefaultTimeInDays());
        obj.setVdsGroupName(instance.getVdsGroupName());
        obj.setAssignedVmsCount(instance.getAssignedVmsCount());
        obj.setVmPoolDescription(instance.getVmPoolDescription());
        obj.setComment(instance.getComment());
        obj.setRunningVmsCount(instance.getRunningVmsCount());
        obj.setPrestartedVms(instance.getPrestartedVms());

        return obj;
    }

    private static StorageDomainStatic cloneStorageDomainStatic(StorageDomainStatic instance)
    {
        StorageDomainStatic obj = new StorageDomainStatic();
        obj.setConnection(instance.getConnection());
        obj.setId(instance.getId());
        obj.setStorage(instance.getStorage());
        obj.setStorageDomainType(instance.getStorageDomainType());
        obj.setStorageType(instance.getStorageType());
        obj.setStorageName(instance.getStorageName());
        obj.setStorageFormat(instance.getStorageFormat());

        return obj;
    }

    private static VmTemplate cloneVmTemplate(VmTemplate instance)
    {
        VmTemplate obj = new VmTemplate();
        obj.setStoragePoolId(instance.getStoragePoolId());
        obj.setStoragePoolName(instance.getStoragePoolName());
        obj.setDefaultDisplayType(instance.getDefaultDisplayType());
        obj.setPriority(instance.getPriority());
        obj.setIsoPath(instance.getIsoPath());
        obj.setOrigin(instance.getOrigin());
        obj.setSizeGB(instance.getSizeGB());
        // TODO: see comments above on DiskImageMap
        obj.setDiskImageMap(instance.getDiskImageMap());
        obj.setInterfaces(instance.getInterfaces());
        obj.setAutoStartup(instance.isAutoStartup());
        obj.setChildCount(instance.getChildCount());
        obj.setCpuPerSocket(instance.getCpuPerSocket());
        obj.setCreationDate(instance.getCreationDate());
        obj.setDefaultBootSequence(instance.getDefaultBootSequence());
        obj.setComment(instance.getComment());
        obj.setFailBack(instance.isFailBack());
        obj.setStateless(instance.isStateless());
        obj.setMemSizeMb(instance.getMemSizeMb());
        obj.setName(instance.getName());
        obj.setNiceLevel(instance.getNiceLevel());
        obj.setNumOfMonitors(instance.getNumOfMonitors());
        obj.setAllowConsoleReconnect(instance.isAllowConsoleReconnect());
        obj.setNumOfSockets(instance.getNumOfSockets());
        obj.setStatus(instance.getStatus());
        obj.setTimeZone(instance.getTimeZone());
        obj.setUsbPolicy(instance.getUsbPolicy());
        obj.setVdsGroupId(instance.getVdsGroupId());
        obj.setVdsGroupName(instance.getVdsGroupName());
        obj.setVmType(instance.getVmType());
        obj.setId(instance.getId());
        obj.setDiskList(instance.getDiskList());
        obj.setRunAndPause(instance.isRunAndPause());
        obj.setClusterArch(instance.getClusterArch());
        obj.setTemplateVersionNumber(instance.getTemplateVersionNumber());
        obj.setBaseTemplateId(instance.getBaseTemplateId());
        obj.setTemplateVersionName(instance.getTemplateVersionName());
        obj.setSerialNumberPolicy(instance.getSerialNumberPolicy());
        obj.setCustomSerialNumber(instance.getCustomSerialNumber());
        obj.setBootMenuEnabled(instance.isBootMenuEnabled());
        obj.setCreatedByUserId(instance.getCreatedByUserId());
        obj.setSpiceFileTransferEnabled(instance.isSpiceFileTransferEnabled());
        obj.setSpiceCopyPasteEnabled(instance.isSpiceCopyPasteEnabled());

        return obj;
    }

    private static VmStatic cloneVmStatic(VmStatic instance)
    {
        VmStatic obj = new VmStatic();

        obj.setFailBack(instance.isFailBack());
        obj.setDefaultBootSequence(instance.getDefaultBootSequence());
        obj.setVmType(instance.getVmType());
        obj.setDefaultDisplayType(instance.getDefaultDisplayType());
        obj.setPriority(instance.getPriority());
        obj.setIsoPath(instance.getIsoPath());
        obj.setOrigin(instance.getOrigin());
        obj.setAutoStartup(instance.isAutoStartup());
        obj.setCpuPerSocket(instance.getCpuPerSocket());
        obj.setCreationDate(instance.getCreationDate());
        obj.setDedicatedVmForVds(instance.getDedicatedVmForVds());
        obj.setDescription(instance.getDescription());
        obj.setComment(instance.getComment());
        obj.setInitialized(instance.isInitialized());
        obj.setStateless(instance.isStateless());
        obj.setRunAndPause(instance.isRunAndPause());
        obj.setMemSizeMb(instance.getMemSizeMb());
        obj.setDiskSize(instance.getDiskSize());
        obj.setNiceLevel(instance.getNiceLevel());
        obj.setNumOfMonitors(instance.getNumOfMonitors());
        obj.setAllowConsoleReconnect(instance.isAllowConsoleReconnect());
        obj.setNumOfSockets(instance.getNumOfSockets());
        obj.setTimeZone(instance.getTimeZone());
        obj.setUsbPolicy(instance.getUsbPolicy());
        obj.setVdsGroupId(instance.getVdsGroupId());
        obj.setId(instance.getId());
        obj.setName(instance.getName());
        obj.setVmtGuid(instance.getVmtGuid());
        obj.setUseLatestVersion(instance.isUseLatestVersion());
        obj.setSerialNumberPolicy(instance.getSerialNumberPolicy());
        obj.setCustomSerialNumber(instance.getCustomSerialNumber());
        obj.setBootMenuEnabled(instance.isBootMenuEnabled());
        obj.setSpiceFileTransferEnabled(instance.isSpiceFileTransferEnabled());
        obj.setSpiceCopyPasteEnabled(instance.isSpiceCopyPasteEnabled());

        return obj;
    }

    private static void cloneNetworkStatisticss(NetworkStatistics instance, NetworkStatistics obj)
    {
        obj.setId(instance.getId());
        obj.setReceiveDropRate(instance.getReceiveDropRate());
        obj.setReceiveRate(instance.getReceiveRate());
        obj.setTransmitDropRate(instance.getTransmitDropRate());
        obj.setTransmitRate(instance.getTransmitRate());
        obj.setStatus(instance.getStatus());
    }

    private static VdsNetworkStatistics cloneVdsNetworkStatistics(VdsNetworkStatistics instance)
    {
        VdsNetworkStatistics obj = new VdsNetworkStatistics();

        cloneNetworkStatisticss(instance, obj);
        obj.setVdsId(instance.getVdsId());

        return obj;
    }

    private static Object cloneVdsNetworkInterface(VdsNetworkInterface vdsNetworkInterface)
    {
        VdsNetworkInterface obj = new VdsNetworkInterface();

        obj.setAddress(vdsNetworkInterface.getAddress());
        obj.setBonded(vdsNetworkInterface.getBonded());
        obj.setBondName(vdsNetworkInterface.getBondName());
        obj.setBondOptions(vdsNetworkInterface.getBondOptions());
        obj.setBondType(vdsNetworkInterface.getBondType());
        obj.setBootProtocol(vdsNetworkInterface.getBootProtocol());
        obj.setGateway(vdsNetworkInterface.getGateway());
        obj.setId(vdsNetworkInterface.getId());
        obj.setMacAddress(vdsNetworkInterface.getMacAddress());
        obj.setName(vdsNetworkInterface.getName());
        obj.setNetworkName(vdsNetworkInterface.getNetworkName());
        obj.setSpeed(vdsNetworkInterface.getSpeed());
        obj.setStatistics(cloneVdsNetworkStatistics(vdsNetworkInterface.getStatistics()));

        return obj;
    }

    private static VmNetworkStatistics cloneVmNetworkStatistics(VmNetworkStatistics instance)
    {
        VmNetworkStatistics obj = new VmNetworkStatistics();

        cloneNetworkStatisticss(instance, obj);
        obj.setVmId(instance.getVmId());

        return obj;
    }

    private static Object cloneVmNetworkInterface(VmNetworkInterface vmNetworkInterface)
    {
        VmNetworkInterface obj = new VmNetworkInterface();
        obj.setId(vmNetworkInterface.getId());
        obj.setMacAddress(vmNetworkInterface.getMacAddress());
        obj.setName(vmNetworkInterface.getName());
        obj.setNetworkName(vmNetworkInterface.getNetworkName());
        obj.setLinked(vmNetworkInterface.isLinked());
        obj.setSpeed(vmNetworkInterface.getSpeed());
        obj.setType(vmNetworkInterface.getType());
        obj.setVmId(vmNetworkInterface.getVmId());
        obj.setVmName(vmNetworkInterface.getVmName());
        obj.setVmTemplateId(vmNetworkInterface.getVmTemplateId());
        obj.setStatistics(cloneVmNetworkStatistics(vmNetworkInterface.getStatistics()));

        return obj;
    }

    private static Object cloneClusterPolicy(ClusterPolicy clusterPolicy) {
        ClusterPolicy obj = new ClusterPolicy();
        if (clusterPolicy.getId() != null) {
            obj.setId(clusterPolicy.getId());
        }
        obj.setName(clusterPolicy.getName());
        obj.setDescription(clusterPolicy.getDescription());
        obj.setLocked(clusterPolicy.isLocked());
        obj.setDefaultPolicy(clusterPolicy.isDefaultPolicy());
        if (clusterPolicy.getFilters() != null) {
            obj.setFilters(new ArrayList<Guid>());
            for (Guid policyUnitId : clusterPolicy.getFilters()) {
                obj.getFilters().add(policyUnitId);
            }
        }
        if (clusterPolicy.getFilterPositionMap() != null) {
            obj.setFilterPositionMap(new HashMap<Guid, Integer>());
            for (Entry<Guid, Integer> entry : clusterPolicy.getFilterPositionMap().entrySet()) {
                obj.getFilterPositionMap().put(entry.getKey(), entry.getValue());
            }
        }
        if (clusterPolicy.getFunctions() != null) {
            obj.setFunctions(new ArrayList<Pair<Guid, Integer>>());
            for (Pair<Guid, Integer> pair : clusterPolicy.getFunctions()) {
                obj.getFunctions().add(new Pair<Guid, Integer>(pair.getFirst(), pair.getSecond()));
            }
        }
        if (clusterPolicy.getBalance() != null) {
            obj.setBalance(clusterPolicy.getBalance());
        }
        if (clusterPolicy.getParameterMap() != null) {
            obj.setParameterMap(new LinkedHashMap());
            for (Entry<String, String> entry : clusterPolicy.getParameterMap().entrySet()) {
                obj.getParameterMap().put(entry.getKey(), entry.getValue());
            }
        }
        return obj;
    }

}
