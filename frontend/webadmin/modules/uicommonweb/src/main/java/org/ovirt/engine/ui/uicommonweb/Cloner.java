package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.Version;

@SuppressWarnings("unused")
public final class Cloner
{
    public static Object clone(Object instance)
    {
        if (instance instanceof VM)
        {
            return CloneVM((VM) instance);
        }
        if (instance instanceof VDS)
        {
            return CloneVDS((VDS) instance);
        }
        if (instance instanceof VDSGroup)
        {
            return CloneVDSGroup((VDSGroup) instance);
        }
        if (instance instanceof storage_pool)
        {
            return CloneStorage_pool((storage_pool) instance);
        }
        if (instance instanceof Network)
        {
            return CloneNetwork((Network) instance);
        }
        if (instance instanceof NetworkCluster)
        {
            return CloneNetworkCluster((NetworkCluster) instance);
        }
        if (instance instanceof VmPool)
        {
            return CloneVmPool((VmPool) instance);
        }
        if (instance instanceof StorageDomainStatic)
        {
            return CloneStorageDomainStatic((StorageDomainStatic) instance);
        }
        if (instance instanceof VmTemplate)
        {
            return CloneVmTemplate((VmTemplate) instance);
        }
        if (instance instanceof VmNetworkInterface)
        {
            return CloneVmNetworkInterface((VmNetworkInterface) instance);
        }
        if (instance instanceof VdsNetworkInterface)
        {
            return CloneVdsNetworkInterface((VdsNetworkInterface) instance);
        }
        if (instance instanceof VmStatic)
        {
            return CloneVmStatic((VmStatic) instance);
        }
        if (instance instanceof Version)
        {
            return CloneVersion((Version) instance);
        }

        // Throw exception to determine development needs.
        throw new NotImplementedException();
    }

    private static Object CloneVM(VM instance)
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
        vm.setElapsedTime(instance.getElapsedTime());
        vm.setRoundedElapsedTime(instance.getRoundedElapsedTime());
        vm.setExitMessage(instance.getExitMessage());
        vm.setExitStatus(instance.getExitStatus());
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
        vm.setAutoSuspend(instance.isAutoSuspend());
        vm.setInitialized(instance.isInitialized());
        vm.setStateless(instance.isStateless());
        vm.setIsoPath(instance.getIsoPath());
        vm.setKernelParams(instance.getKernelParams());
        vm.setKernelUrl(instance.getKernelUrl());
        vm.setKvmEnable(instance.getKvmEnable());
        // TODO: Guid/NGuid is an object, but code should treat it as immutable, and not change it's uuid directly.
        // (quick skim of code shows this should be safe with current code)
        vm.setLastVdsRunOn(instance.getLastVdsRunOn());
        vm.setMigratingToVds(instance.getmigrating_to_vds());
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
        vm.setVmDomain(instance.getVmDomain());
        vm.setId(instance.getId());
        vm.setVmHost(instance.getVmHost());
        vm.setVmIp(instance.getVmIp());
        vm.setLastStartTime(instance.getLastStartTime());
        vm.setVmMemSizeMb(instance.getVmMemSizeMb());
        vm.setVmName(instance.getVmName());
        vm.setVmOs(instance.getVmOs());
        vm.setVmPid(instance.getVmPid());
        vm.setVmType(instance.getVmType());
        vm.setVmPoolId(instance.getVmPoolId());
        vm.setVmPoolName(instance.getVmPoolName());
        vm.setVmtGuid(instance.getVmtGuid());
        vm.setVmtName(instance.getVmtName());

        return vm;
    }

    private static Object CloneVersion(Version instance)
    {
        return new Version(instance.toString());
    }

    private static Object CloneVDS(VDS instance)
    {
        VDS obj = new VDS();

        obj.sethost_name(instance.gethost_name());

        obj.setManagmentIp(instance.getManagmentIp());
        obj.setpm_user(instance.getpm_user());
        obj.setpm_password(instance.getpm_password());
        obj.setpm_type(instance.getpm_type());
        obj.setPmOptionsMap(instance.getPmOptionsMap());

        obj.setPmSecondaryIp(instance.getManagmentIp());
        obj.setPmSecondaryUser(instance.getpm_user());
        obj.setPmSecondaryPassword(instance.getpm_password());
        obj.setPmSecondaryType(instance.getpm_type());
        obj.setPmSecondaryOptionsMap(instance.getPmOptionsMap());

        obj.setpm_enabled(instance.getpm_enabled());
        obj.setPmSecondaryConcurrent(instance.isPmSecondaryConcurrent());

        obj.setport(instance.getport());
        obj.setserver_SSL_enabled(instance.getserver_SSL_enabled());
        obj.setvds_group_id(instance.getvds_group_id());
        obj.setId(instance.getId());
        obj.setvds_name(instance.getvds_name());
        obj.setvds_strength(instance.getvds_strength());
        obj.setvds_type(instance.getvds_type());
        obj.setUniqueId(instance.getUniqueId());
        obj.setVdsSpmPriority(instance.getVdsSpmPriority());

        return obj;
    }

    private static storage_pool CloneStorage_pool(storage_pool instance)
    {
        storage_pool obj = new storage_pool();

        obj.setdescription(instance.getdescription());
        obj.setId(instance.getId());
        obj.setname(instance.getname());
        obj.setstorage_pool_type(instance.getstorage_pool_type());
        obj.setstatus(instance.getstatus());

        obj.setmaster_domain_version(instance.getmaster_domain_version());
        obj.setLVER(instance.getLVER());
        obj.setrecovery_mode(instance.getrecovery_mode());
        obj.setspm_vds_id(instance.getspm_vds_id());
        obj.setcompatibility_version(instance.getcompatibility_version());

        return obj;
    }

    private static VDSGroup CloneVDSGroup(VDSGroup instance)
    {
        VDSGroup obj = new VDSGroup();
        obj.setId(instance.getId());
        obj.setname(instance.getname());
        obj.setdescription(instance.getdescription());
        obj.setcpu_name(instance.getcpu_name());

        obj.setselection_algorithm(instance.getselection_algorithm());
        obj.sethigh_utilization(instance.gethigh_utilization());
        obj.setlow_utilization(instance.getlow_utilization());
        obj.setcpu_over_commit_duration_minutes(instance.getcpu_over_commit_duration_minutes());
        obj.setcompatibility_version(instance.getcompatibility_version());
        obj.setMigrateOnError(instance.getMigrateOnError());
        obj.setTransparentHugepages(instance.getTransparentHugepages());

        obj.setStoragePoolId(instance.getStoragePoolId());
        obj.setmax_vds_memory_over_commit(instance.getmax_vds_memory_over_commit());
        obj.setCountThreadsAsCores(instance.getCountThreadsAsCores());

        return obj;
    }

    private static Network CloneNetwork(Network instance)
    {
        Network obj = new Network();

        obj.setAddr(instance.getAddr());
        obj.setDescription(instance.getDescription());
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
            obj.setCluster(CloneNetworkCluster(instance.getCluster()));
        }
        return obj;
    }

    private static NetworkCluster CloneNetworkCluster(NetworkCluster instance)
    {
        NetworkCluster obj = new NetworkCluster();

        obj.setStatus(instance.getStatus());
        obj.setDisplay(instance.isDisplay());
        obj.setRequired(instance.isRequired());
        return obj;
    }

    private static VmPool CloneVmPool(VmPool instance)
    {
        VmPool obj = new VmPool();

        obj.setvm_pool_description(instance.getvm_pool_description());
        obj.setvm_pool_id(instance.getvm_pool_id());
        obj.setvm_pool_name(instance.getvm_pool_name());
        obj.setvm_pool_type(instance.getvm_pool_type());
        obj.setvds_group_id(instance.getvds_group_id());

        obj.setvm_pool_type(instance.getvm_pool_type());
        obj.setparameters(instance.getparameters());
        obj.setDefaultEndTime(instance.getDefaultEndTime());
        obj.setDefaultStartTime(instance.getDefaultStartTime());
        obj.setDefaultTimeInDays(instance.getDefaultTimeInDays());
        obj.setvds_group_name(instance.getvds_group_name());
        obj.setvm_assigned_count(instance.getvm_assigned_count());
        obj.setvm_pool_description(instance.getvm_pool_description());
        obj.setvm_running_count(instance.getvm_running_count());
        obj.setPrestartedVms(instance.getPrestartedVms());

        return obj;
    }

    private static StorageDomainStatic CloneStorageDomainStatic(StorageDomainStatic instance)
    {
        StorageDomainStatic obj = new StorageDomainStatic();
        obj.setConnection(instance.getConnection());
        obj.setId(instance.getId());
        obj.setstorage(instance.getstorage());
        obj.setstorage_domain_type(instance.getstorage_domain_type());
        obj.setstorage_type(instance.getstorage_type());
        obj.setstorage_name(instance.getstorage_name());
        obj.setStorageFormat(instance.getStorageFormat());

        return obj;
    }

    private static VmTemplate CloneVmTemplate(VmTemplate instance)
    {
        VmTemplate obj = new VmTemplate();
        obj.setstorage_pool_id(instance.getstorage_pool_id());
        obj.setstorage_pool_name(instance.getstorage_pool_name());
        obj.setDefaultDisplayType(instance.getDefaultDisplayType());
        obj.setPriority(instance.getPriority());
        obj.setIsoPath(instance.getIsoPath());
        obj.setOrigin(instance.getOrigin());
        obj.setSizeGB(instance.getSizeGB());
        // TODO: see comments above on DiskImageMap
        obj.setDiskImageMap(instance.getDiskImageMap());
        obj.setInterfaces(instance.getInterfaces());
        obj.setAutoStartup(instance.isAutoStartup());
        obj.setchild_count(instance.getchild_count());
        obj.setCpuPerSocket(instance.getCpuPerSocket());
        obj.setCreationDate(instance.getCreationDate());
        obj.setDefaultBootSequence(instance.getDefaultBootSequence());
        obj.setDescription(instance.getDescription());
        obj.setDomain(instance.getDomain());
        obj.setFailBack(instance.isFailBack());
        obj.setAutoSuspend(instance.isAutoSuspend());
        obj.setStateless(instance.isStateless());
        obj.setMemSizeMb(instance.getMemSizeMb());
        obj.setname(instance.getname());
        obj.setNiceLevel(instance.getNiceLevel());
        obj.setNumOfMonitors(instance.getNumOfMonitors());
        obj.setAllowConsoleReconnect(instance.isAllowConsoleReconnect());
        obj.setNumOfSockets(instance.getNumOfSockets());
        obj.setstatus(instance.getstatus());
        obj.setTimeZone(instance.getTimeZone());
        obj.setUsbPolicy(instance.getUsbPolicy());
        obj.setVdsGroupId(instance.getVdsGroupId());
        obj.setvds_group_name(instance.getvds_group_name());
        obj.setVmType(instance.getVmType());
        obj.setId(instance.getId());
        obj.setDiskList(instance.getDiskList());

        return obj;
    }

    private static VmStatic CloneVmStatic(VmStatic instance)
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
        obj.setDomain(instance.getDomain());
        obj.setAutoSuspend(instance.isAutoSuspend());
        obj.setInitialized(instance.isInitialized());
        obj.setStateless(instance.isStateless());
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
        obj.setVmName(instance.getVmName());
        obj.setVmtGuid(instance.getVmtGuid());

        return obj;
    }

    private static void CloneNetworkStatisticss(NetworkStatistics instance, NetworkStatistics obj)
    {
        obj.setId(instance.getId());
        obj.setReceiveDropRate(instance.getReceiveDropRate());
        obj.setReceiveRate(instance.getReceiveRate());
        obj.setTransmitDropRate(instance.getTransmitDropRate());
        obj.setTransmitRate(instance.getTransmitRate());
        obj.setStatus(instance.getStatus());
    }

    private static VdsNetworkStatistics CloneVdsNetworkStatistics(VdsNetworkStatistics instance)
    {
        VdsNetworkStatistics obj = new VdsNetworkStatistics();

        CloneNetworkStatisticss(instance, obj);
        obj.setVdsId(instance.getVdsId());

        return obj;
    }

    private static Object CloneVdsNetworkInterface(VdsNetworkInterface vdsNetworkInterface)
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
        obj.setStatistics(CloneVdsNetworkStatistics(vdsNetworkInterface.getStatistics()));

        return obj;
    }

    private static VmNetworkStatistics CloneVmNetworkStatistics(VmNetworkStatistics instance)
    {
        VmNetworkStatistics obj = new VmNetworkStatistics();

        CloneNetworkStatisticss(instance, obj);
        obj.setVmId(instance.getVmId());

        return obj;
    }

    private static Object CloneVmNetworkInterface(VmNetworkInterface vmNetworkInterface)
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
        obj.setStatistics(CloneVmNetworkStatistics(vmNetworkInterface.getStatistics()));

        return obj;
    }
}
