package org.ovirt.engine.core.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.AutoNumaBalanceStatus;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code VdsDaoImpl} provides an implementation of {@link VdsDao}.
 */
@Named
@Singleton
public class VdsDynamicDaoImpl extends MassOperationsGenericDao<VdsDynamic, Guid> implements VdsDynamicDao {

    @Inject
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;

    private static final Logger log = LoggerFactory.getLogger(VdsDynamicDaoImpl.class);

    public VdsDynamicDaoImpl() {
        super("VdsDynamic");
    }

    private final RowMapper<VdsDynamic> vdsDynamicRowMapper = (rs, rowNum) -> {
        VdsDynamic entity = new VdsDynamic();
        entity.setCpuCores((Integer) rs.getObject("cpu_cores"));
        entity.setCpuThreads((Integer) rs.getObject("cpu_threads"));
        entity.setCpuModel(rs.getString("cpu_model"));
        entity.setOnlineCpus(rs.getString("online_cpus"));
        entity.setCpuSpeedMh(rs.getDouble("cpu_speed_mh"));
        entity.setIfTotalSpeed(rs.getString("if_total_speed"));
        entity.setKvmEnabled((Boolean) rs.getObject("kvm_enabled"));
        entity.setMemCommited((Integer) rs.getObject("mem_commited"));
        entity.setPhysicalMemMb((Integer) rs.getObject("physical_mem_mb"));
        entity.setStatus(VDSStatus.forValue(rs.getInt("status")));

        Guid hostId = getGuidDefaultEmpty(rs, "vds_id");
        entity.setId(hostId);

        entity.setVmActive((Integer) rs.getObject("vm_active"));
        entity.setVmCount(rs.getInt("vm_count"));
        entity.setVmsCoresCount(rs.getInt("vms_cores_count"));
        entity.setVmMigrating((Integer) rs.getObject("vm_migrating"));
        entity.setIncomingMigrations(rs.getInt("incoming_migrations"));
        entity.setOutgoingMigrations(rs.getInt("outgoing_migrations"));
        entity.setReservedMem((Integer) rs.getObject("reserved_mem"));
        entity.setGuestOverhead(rs.getInt("guest_overhead"));
        entity.setSoftwareVersion(rs.getString("software_version"));
        entity.setVersionName(rs.getString("version_name"));
        entity.setVersion(new RpmVersion(rs.getString("rpm_version")));
        entity.setBuildName(rs.getString("build_name"));
        entity.setPreviousStatus(VDSStatus.forValue(rs.getInt("previous_status")));
        entity.setCpuFlags(rs.getString("cpu_flags"));
        entity.setPendingVcpusCount((Integer) rs.getObject("pending_vcpus_count"));
        entity.setPendingVmemSize(rs.getInt("pending_vmem_size"));
        entity.setCpuSockets((Integer) rs.getObject("cpu_sockets"));
        entity.setNetConfigDirty((Boolean) rs.getObject("net_config_dirty"));
        entity.setSupportedClusterLevels(rs.getString("supported_cluster_levels"));
        entity.setSupportedEngines(rs.getString("supported_engines"));
        entity.setHostOs(rs.getString("host_os"));
        entity.setKvmVersion(rs.getString("kvm_version"));
        entity.setLibvirtVersion(new RpmVersion(rs.getString("libvirt_version")));
        entity.setSpiceVersion(rs.getString("spice_version"));
        entity.setGlusterVersion(new RpmVersion(rs.getString("gluster_version")));
        entity.setLibrbdVersion(new RpmVersion(rs.getString("librbd1_version")));
        entity.setGlusterfsCliVersion(new RpmVersion(rs.getString("glusterfs_cli_version")));
        entity.setOvsVersion(new RpmVersion(rs.getString("openvswitch_version")));
        entity.setNmstateVersion(new RpmVersion(rs.getString("nmstate_version")));
        entity.setKernelVersion(rs.getString("kernel_version"));
        entity.setIScsiInitiatorName(rs.getString("iscsi_initiator_name"));
        entity.setTransparentHugePagesState(VdsTransparentHugePagesState
                .forValue(rs.getInt("transparent_hugepages_state")));
        entity.setHooksStr(rs.getString("hooks"));
        entity.setNonOperationalReason(NonOperationalReason.forValue(rs.getInt("non_operational_reason")));
        entity.setHardwareManufacturer(rs.getString("hw_manufacturer"));
        entity.setHardwareProductName(rs.getString("hw_product_name"));
        entity.setHardwareVersion(rs.getString("hw_version"));
        entity.setHardwareSerialNumber(rs.getString("hw_serial_number"));
        entity.setHardwareUUID(rs.getString("hw_uuid"));
        entity.setHardwareFamily(rs.getString("hw_family"));
        entity.setHBAs(new JsonObjectDeserializer().deserialize(rs.getString("hbas"), HashMap.class));
        entity.setPowerManagementControlledByPolicy(rs.getBoolean("controlled_by_pm_policy"));
        entity.setKdumpStatus(KdumpStatus.valueOfNumber(rs.getInt("kdump_status")));
        entity.setSELinuxEnforceMode((Integer) rs.getObject("selinux_enforce_mode"));
        entity.setAutoNumaBalancing(AutoNumaBalanceStatus.forValue(rs.getInt("auto_numa_balancing")));
        entity.setNumaSupport(rs.getBoolean("is_numa_supported"));
        entity.setSupportedEmulatedMachines(rs.getString("supported_emulated_machines"));
        entity.getSupportedRngSources().addAll(VmRngDevice.csvToSourcesSet(rs.getString("supported_rng_sources")));
        entity.setMaintenanceReason(rs.getString("maintenance_reason"));
        entity.setUpdateAvailable(rs.getBoolean("is_update_available"));
        entity.setExternalStatus(ExternalStatus.forValue(rs.getInt("external_status")));
        entity.setHostDevicePassthroughEnabled(rs.getBoolean("is_hostdev_enabled"));
        entity.setKernelArgs(rs.getString("kernel_args"));
        entity.setPrettyName(rs.getString("pretty_name"));
        entity.setHostedEngineConfigured(rs.getBoolean("hosted_engine_configured"));

        entity.setInFenceFlow(rs.getBoolean("in_fence_flow"));
        entity.setKernelFeatures(
                ObjectUtils.mapNullable(rs.getString("kernel_features"), JsonHelper::jsonToMapUnchecked));
        entity.setVncEncryptionEnabled(rs.getBoolean("vnc_encryption_enabled"));
        entity.setConnectorInfo(
                ObjectUtils.mapNullable(rs.getString("connector_info"), JsonHelper::jsonToMapUnchecked));
        entity.setBackupEnabled(rs.getBoolean("backup_enabled"));
        entity.setColdBackupEnabled(rs.getBoolean("cold_backup_enabled"));
        entity.setClearBitmapsEnabled(rs.getBoolean("clear_bitmaps_enabled"));
        entity.setSupportedDomainVersionsAsString(rs.getString("supported_domain_versions"));
        entity.setSupportedBlockSize(ObjectUtils.mapNullable(
                rs.getString("supported_block_size"), JsonHelper::jsonToMapUnchecked));
        entity.setTscFrequency(rs.getString("tsc_frequency"));
        entity.setTscScalingEnabled(rs.getBoolean("tsc_scaling"));
        entity.setFipsEnabled(rs.getBoolean("fips_enabled"));
        entity.setBootUuid(rs.getString("boot_uuid"));
        entity.setCdChangePdiv(rs.getBoolean("cd_change_pdiv"));
        entity.setOvnConfigured(rs.getBoolean("ovn_configured"));

        return entity;
    };

    @Override
    public VdsDynamic get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        return getCallsHandler().executeRead("GetVdsDynamicByVdsId", vdsDynamicRowMapper, parameterSource);
    }

    @Override
    public void save(VdsDynamic vds) {
        getCallsHandler().executeModification("InsertVdsDynamic", createFullParametersMapperForSave(vds));
    }

    @Override
    public void update(VdsDynamic vds) {
        getCallsHandler().executeModification("UpdateVdsDynamic", createFullParametersMapper(vds));
    }

    public void updateDnsResolverConfiguration(Guid vdsId, DnsResolverConfiguration reportedDnsResolverConfiguration) {
        if (reportedDnsResolverConfiguration == null) {
            dnsResolverConfigurationDao.removeByVdsDynamicId(vdsId);
        } else {
            if (reportedDnsResolverConfiguration.getId() == null) {
                reportedDnsResolverConfiguration.setId(vdsId);
                dnsResolverConfigurationDao.save(reportedDnsResolverConfiguration);
            } else {
                Validate.isTrue(Objects.equals(vdsId, reportedDnsResolverConfiguration.getId()));
                dnsResolverConfigurationDao.update(reportedDnsResolverConfiguration);
            }
        }
    }

    @Override
    public void remove(Guid id) {
        dnsResolverConfigurationDao.removeByVdsDynamicId(id);

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        getCallsHandler().executeModification("DeleteVdsDynamic", parameterSource);
    }

    @Override
    public List<VdsDynamic> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateStatus(Guid id, VDSStatus status) {
        MapSqlParameterSource parameterSource = getStatusSqlParameterSource(id, status);

        getCallsHandler().executeModification("UpdateVdsDynamicStatus", parameterSource);
    }

    @Override
    public void updateStatusAndReasons(VdsDynamic host) {
        MapSqlParameterSource parameterSource = getStatusSqlParameterSource(host.getId(), host.getStatus());
        parameterSource.addValue("non_operational_reason", host.getNonOperationalReason())
                .addValue("maintenance_reason", host.getMaintenanceReason());

        getCallsHandler().executeModification("UpdateVdsDynamicStatusAndReasons", parameterSource);
    }

    @Override
    public boolean checkIfExistsHostWithStatusInCluster(Guid clusterId, VDSStatus hostStatus) {
        final MapSqlParameterSource customMapSqlParameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId)
                .addValue("host_status", hostStatus);
        return getCallsHandler().executeRead(
                "CheckIfExistsHostWithStatusInCluster",
                SingleColumnRowMapper.newInstance(Boolean.class),
                customMapSqlParameterSource);
    }

    private MapSqlParameterSource getStatusSqlParameterSource(Guid id, VDSStatus status) {
        return getCustomMapSqlParameterSource()
                .addValue("vds_guid", id)
                .addValue("status", status);
    }

    public void updateExternalStatus(Guid id, ExternalStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_guid", id)
                .addValue("external_status", status);

        getCallsHandler().executeModification("UpdateHostExternalStatus", parameterSource);
    }

    @Override
    public void updateNetConfigDirty(Guid id, Boolean netConfigDirty) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_guid", id)
                .addValue("net_config_dirty", netConfigDirty);

        getCallsHandler().executeModification("UpdateVdsDynamicNetConfigDirty", parameterSource);
    }

    @Override
    public void updateVdsDynamicPowerManagementPolicyFlag(Guid id,
                                                          boolean controlledByPmPolicy) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id)
                .addValue("controlled_by_pm_policy", controlledByPmPolicy);

        getCallsHandler().executeModification("UpdateVdsDynamicPowerManagementPolicyFlag", parameterSource);

    }

    private MapSqlParameterSource createFullParametersMapperForSave(VdsDynamic vds) {
        MapSqlParameterSource parameterSource = createFullParametersMapper(vds);
        parameterSource.addValue("controlled_by_pm_policy", vds.isPowerManagementControlledByPolicy());
        return parameterSource;
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VdsDynamic vds) {
        return getCustomMapSqlParameterSource()
                .addValue("cpu_cores", vds.getCpuCores())
                .addValue("cpu_threads", vds.getCpuThreads())
                .addValue("cpu_model", vds.getCpuModel())
                .addValue("online_cpus", vds.getOnlineCpus())
                .addValue("cpu_speed_mh", vds.getCpuSpeedMh())
                .addValue("if_total_speed", vds.getIfTotalSpeed())
                .addValue("kvm_enabled", vds.getKvmEnabled())
                .addValue("mem_commited", vds.getMemCommited())
                .addValue("physical_mem_mb", vds.getPhysicalMemMb())
                .addValue("status", vds.getStatus())
                .addValue("vds_id", vds.getId())
                .addValue("vm_active", vds.getVmActive())
                .addValue("vm_count", vds.getVmCount())
                .addValue("vms_cores_count", vds.getVmsCoresCount())
                .addValue("vm_migrating", vds.getVmMigrating())
                .addValue("incoming_migrations", vds.getIncomingMigrations())
                .addValue("outgoing_migrations", vds.getOutgoingMigrations())
                .addValue("reserved_mem", vds.getReservedMem())
                .addValue("guest_overhead", vds.getGuestOverhead())
                .addValue("rpm_version", vds.getVersion().getRpmName())
                .addValue("software_version", vds.getSoftwareVersion())
                .addValue("version_name", vds.getVersionName())
                .addValue("build_name", vds.getBuildName())
                .addValue("previous_status", vds.getPreviousStatus())
                .addValue("cpu_flags", vds.getCpuFlags())
                .addValue("pending_vcpus_count", vds.getPendingVcpusCount())
                .addValue("pending_vmem_size", vds.getPendingVmemSize())
                .addValue("cpu_sockets", vds.getCpuSockets())
                .addValue("net_config_dirty", vds.getNetConfigDirty())
                .addValue("supported_cluster_levels",
                        vds.getSupportedClusterLevels())
                .addValue("supported_engines", vds.getSupportedEngines())
                .addValue("host_os", vds.getHostOs())
                .addValue("kvm_version", vds.getKvmVersion())
                .addValue("libvirt_version", vds.getLibvirtVersion().getRpmName())
                .addValue("spice_version", vds.getSpiceVersion())
                .addValue("gluster_version", vds.getGlusterVersion().getRpmName())
                .addValue("librbd1_version", vds.getLibrbdVersion().getRpmName())
                .addValue("glusterfs_cli_version", vds.getGlusterfsCliVersion().getRpmName())
                .addValue("openvswitch_version", vds.getOvsVersion().getRpmName())
                .addValue("nmstate_version", vds.getNmstateVersion().getRpmName())
                .addValue("kernel_version", vds.getKernelVersion())
                .addValue("iscsi_initiator_name", vds.getIScsiInitiatorName())
                .addValue("transparent_hugepages_state",
                        vds.getTransparentHugePagesState().getValue())
                .addValue("hooks", vds.getHooksStr())
                .addValue("non_operational_reason",
                        vds.getNonOperationalReason().getValue())
                .addValue("hw_manufacturer", vds.getHardwareManufacturer())
                .addValue("hw_product_name", vds.getHardwareProductName())
                .addValue("hw_version", vds.getHardwareVersion())
                .addValue("hw_serial_number", vds.getHardwareSerialNumber())
                .addValue("hw_uuid", vds.getHardwareUUID())
                .addValue("hw_family", vds.getHardwareFamily())
                .addValue("hbas", new JsonObjectSerializer().serialize(vds.getHBAs()))
                .addValue("supported_emulated_machines", vds.getSupportedEmulatedMachines())
                .addValue("kdump_status", vds.getKdumpStatus().getAsNumber())
                .addValue("selinux_enforce_mode", (vds.getSELinuxEnforceMode() != null)
                                                  ? vds.getSELinuxEnforceMode().toInt()
                                                  : null)
                .addValue("auto_numa_balancing", vds.getAutoNumaBalancing().getValue())
                .addValue("is_numa_supported", vds.isNumaSupport())
                .addValue("supported_rng_sources", VmRngDevice.sourcesToCsv(vds.getSupportedRngSources()))
                .addValue("supported_emulated_machines", vds.getSupportedEmulatedMachines())
                .addValue("maintenance_reason", vds.getMaintenanceReason())
                .addValue("is_update_available", vds.isUpdateAvailable())
                .addValue("kernel_args", vds.getKernelArgs())
                .addValue("is_hostdev_enabled", vds.isHostDevicePassthroughEnabled())
                .addValue("pretty_name", vds.getPrettyName())
                .addValue("hosted_engine_configured", vds.isHostedEngineConfigured())
                .addValue("in_fence_flow", vds.isInFenceFlow())
                .addValue("kernel_features",
                        ObjectUtils.mapNullable(vds.getKernelFeatures(), JsonHelper::mapToJsonUnchecked))
                .addValue("vnc_encryption_enabled", vds.isVncEncryptionEnabled())
                .addValue("connector_info",
                    ObjectUtils.mapNullable(vds.getConnectorInfo(), JsonHelper::mapToJsonUnchecked))
                .addValue("backup_enabled", vds.isBackupEnabled())
                .addValue("cold_backup_enabled", vds.isColdBackupEnabled())
                .addValue("clear_bitmaps_enabled", vds.isClearBitmapsEnabled())
                .addValue("supported_domain_versions", vds.getSupportedDomainVersionsAsString())
                .addValue("supported_block_size",
                        ObjectUtils.mapNullable(vds.getSupportedBlockSize(), JsonHelper::mapToJsonUnchecked))
                .addValue("tsc_frequency", vds.getTscFrequency())
                .addValue("tsc_scaling", vds.isTscScalingEnabled())
                .addValue("fips_enabled", vds.isFipsEnabled())
                .addValue("boot_uuid", vds.getBootUuid())
                .addValue("cd_change_pdiv", vds.isCdChangePdiv())
                .addValue("ovn_configured", vds.isOvnConfigured());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("vds_id", id);
    }

    @Override
    protected RowMapper<VdsDynamic> createEntityRowMapper() {
        return vdsDynamicRowMapper;
    }

    @Override
    public void updateIfNeeded(VdsDynamic vdsDynamic) {
        VdsDynamic dbData = get(vdsDynamic.getId());
        if (!dbData.equals(vdsDynamic)) {
            update(vdsDynamic);
        } else {
            log.debug("Ignored an unneeded update of VdsDynamic");
        }
    }

    @Override
    public void updateCpuFlags(Guid id, String cpuFlags) {
        getCallsHandler().executeModification(
                "updateCpuFlags",
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id)
                        .addValue("cpu_flags", cpuFlags));
    }

    @Override
    public List<Guid> getIdsOfHostsWithStatus(VDSStatus status) {
        return getCallsHandler().executeReadList("GetIdsOfHostsWithStatus",
                createGuidMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("status", status.getValue()));
    }

    @Override
    public void updateUpdateAvailable(Guid id, boolean updateAvailable) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_guid", id)
                .addValue("is_update_available", updateAvailable);

        getCallsHandler().executeModification("UpdateVdsDynamicIsUpdateAvailable", parameterSource);
    }
}
