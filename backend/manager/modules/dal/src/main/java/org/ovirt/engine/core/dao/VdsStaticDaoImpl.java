package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@code VdsDaoImpl} provides an implementation of {@link VdsDao}.
 */
@Named
@Singleton
public class VdsStaticDaoImpl extends BaseDao implements VdsStaticDao {

    private static final Logger log = LoggerFactory.getLogger(VdsStaticDaoImpl.class);

    @Override
    public VdsStatic get(Guid id) {
        return getCallsHandler().executeRead("GetVdsStaticByVdsId",
                vdsStaticRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
    }

    @Override
    public VdsStatic getByHostName(String host) {
        return getCallsHandler().executeRead("GetVdsStaticByHostName",
                vdsStaticRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("host_name", host));
    }

    @Override
    public List<VdsStatic> getAllWithIpAddress(String address) {
        return getCallsHandler().executeReadList("GetVdsStaticByIp",
                vdsStaticRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("ip", address));
    }

    @Override
    public List<VdsStatic> getAllForCluster(Guid cluster) {
        return getCallsHandler().executeReadList("GetVdsStaticByClusterId",
                vdsStaticRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", cluster));
    }

    @Override
    public void save(VdsStatic vds) {
        Guid id = vds.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            vds.setId(id);
        }
        new SimpleJdbcCall(getJdbcTemplate()).withProcedureName("InsertVdsStatic")
                .execute(getInsertOrUpdateParams(vds));
    }

    /**
     * Note: Id doesn't update {@code last_stored_kernel_cmdline} column.
     * @see #updateLastStoredKernelCmdline(Guid, String)
     */
    @Override
    public void update(VdsStatic vds) {
        getCallsHandler().executeModification("UpdateVdsStatic", getInsertOrUpdateParams(vds));
    }

    private MapSqlParameterSource getInsertOrUpdateParams(final VdsStatic vds) {
        return getCustomMapSqlParameterSource()
                .addValue("host_name", vds.getHostName())
                .addValue("free_text_comment", vds.getComment())
                .addValue("vds_unique_id", vds.getUniqueID())
                .addValue("port", vds.getPort())
                .addValue("cluster_id", vds.getClusterId())
                .addValue("vds_id", vds.getId())
                .addValue("vds_name", vds.getName())
                .addValue("server_SSL_enabled", vds.isServerSslEnabled())
                .addValue("vds_type", vds.getVdsType())
                .addValue("pm_enabled", vds.isPmEnabled())
                .addValue("pm_proxy_preferences", FenceProxySourceTypeHelper.saveAsString(vds.getFenceProxySources()))
                .addValue("pm_detect_kdump", vds.isPmKdumpDetection())
                .addValue("otp_validity", vds.getOtpValidity())
                .addValue("vds_spm_priority", vds.getVdsSpmPriority())
                .addValue("console_address", vds.getConsoleAddress())
                .addValue("sshKeyFingerprint", vds.getSshKeyFingerprint())
                .addValue("ssh_public_key", vds.getSshPublicKey())
                .addValue("ssh_port", vds.getSshPort())
                .addValue("ssh_username", vds.getSshUsername())
                .addValue("disable_auto_pm", vds.isDisablePowerManagementPolicy())
                .addValue("host_provider_id", vds.getHostProviderId())
                .addValue("kernel_cmdline", KernelCmdlineColumn.fromVdsStatic(vds).toJson())
                .addValue("last_stored_kernel_cmdline", vds.getLastStoredKernelCmdline())
                .addValue("reinstall_required", vds.isReinstallRequired())
                .addValue("vgpu_placement", vds.getVgpuPlacement());
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        getCallsHandler().executeModification("DeleteVdsStatic", parameterSource);
    }

    @Override
    public List<VdsStatic> getAll() {
        throw new UnsupportedOperationException();
    }

    private static final RowMapper<VdsStatic> vdsStaticRowMapper = (rs, rowNum) -> {
        VdsStatic entity = new VdsStatic();
        entity.setHostName(rs.getString("host_name"));
        entity.setComment(rs.getString("free_text_comment"));
        entity.setUniqueID(rs.getString("vds_unique_id"));
        entity.setPort(rs.getInt("port"));
        entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        entity.setId(getGuidDefaultEmpty(rs, "vds_id"));
        entity.setSshPort(rs.getInt("ssh_port"));
        entity.setSshUsername(rs.getString("ssh_username"));
        entity.setClusterId(Guid.createGuidFromStringDefaultEmpty(rs.getString("cluster_id")));
        entity.setId(Guid.createGuidFromStringDefaultEmpty(rs.getString("vds_id")));
        entity.setName(rs.getString("vds_name"));
        entity.setServerSslEnabled(rs.getBoolean("server_SSL_enabled"));
        entity.setVdsType(VDSType.forValue(rs.getInt("vds_type")));
        entity.setPmEnabled(rs.getBoolean("pm_enabled"));
        entity.setFenceProxySources(FenceProxySourceTypeHelper.parseFromString(rs.getString("pm_proxy_preferences")));
        entity.setPmKdumpDetection(rs.getBoolean("pm_detect_kdump"));
        entity.setOtpValidity(rs.getLong("otp_validity"));
        entity.setSshKeyFingerprint(rs.getString("sshKeyFingerprint"));
        entity.setSshPublicKey(rs.getString("ssh_public_key"));
        entity.setConsoleAddress(rs.getString("console_address"));
        entity.setDisablePowerManagementPolicy(rs.getBoolean("disable_auto_pm"));
        entity.setHostProviderId(getGuid(rs, "host_provider_id"));
        KernelCmdlineColumn.fromJson(rs.getString("kernel_cmdline")).toVdsStatic(entity);
        entity.setLastStoredKernelCmdline(rs.getString("last_stored_kernel_cmdline"));
        entity.setReinstallRequired(rs.getBoolean("reinstall_required"));
        entity.setVgpuPlacement(rs.getInt("vgpu_placement"));
        return entity;
    };

    @Override
    public VdsStatic getByVdsName(String vdsName) {
        return getCallsHandler().executeRead("GetVdsStaticByVdsName",
                vdsStaticRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("host_name", vdsName));
    }

    @Override
    public List<String> getAllHostNamesPinnedToVm(Guid vm) {
        return getCallsHandler().executeReadList("GetNamesOfHostsDedicatedToVm",
                SingleColumnRowMapper.newInstance(String.class),
                getCustomMapSqlParameterSource().addValue("vm_guid", vm));
    }

    @Override
    public void updateLastStoredKernelCmdline(Guid vdsStaticId, String lastStoredKernelCmdline) {
        getCallsHandler().executeModification(
                "UpdateVdsStaticLastStoredKernelCmdline",
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", vdsStaticId)
                        .addValue("last_stored_kernel_cmdline", lastStoredKernelCmdline));
    }

    @Override
    public void updateKernelCmdlines(Guid vdsStaticId, VdsStatic staticData) {
        String kernelCmdline = KernelCmdlineColumn.fromVdsStatic(staticData).toJson();
        getCallsHandler().executeModification(
                "UpdateVdsStaticKernelCmdlines",
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", vdsStaticId)
                        .addValue("kernel_cmdline", kernelCmdline));
    }

    @Override
    public void updateReinstallRequired(Guid vdsStaticId, boolean reinstallRequired) {
        getCallsHandler().executeModification(
                "UpdateVdsReinstallRequired",
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", vdsStaticId)
                        .addValue("reinstall_required", reinstallRequired));
    }

    @Override
    public boolean checkIfExistsHostThatMissesNetworkInCluster(Guid clusterId,
            String networkName,
            VDSStatus hostStatus) {
        final MapSqlParameterSource customMapSqlParameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId)
                .addValue("network_name", networkName)
                .addValue("host_status", hostStatus);
        return getCallsHandler().executeRead(
                "CheckIfExistsHostThatMissesNetworkInCluster",
                SingleColumnRowMapper.newInstance(Boolean.class),
                customMapSqlParameterSource);
    }

    @Override
    public List<VdsStatic> getByIds(List<Guid> ids) {
        return getCallsHandler().executeReadList("GetVdsStaticByVdsIds",
                vdsStaticRowMapper,
                getCustomMapSqlParameterSource().addValue("vds_ids", createArrayOfUUIDs(ids)));
    }

    /**
     * Model of JSON structured column "kernel_cmdline"
     */
    static class KernelCmdlineColumn implements Serializable {

        private String current;
        private boolean parsable;
        private boolean blacklistNouveau;
        private boolean iommu;
        private boolean kvmNested;
        private boolean unsafeInterrupts;
        private boolean pciRealloc;
        private boolean fips;
        private boolean smtDisabled;

        public String toJson() {
            try {
                return new ObjectMapper()
                        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                        .setSerializationInclusion(JsonInclude.Include.ALWAYS)
                        .writeValueAsString(this);
            } catch (JsonProcessingException e) {
                log.error("Cannot serialize {} because {}", this, ExceptionUtils.getRootCauseMessage(e));
                log.debug("Cannot serialize {}. Details {}", this, ExceptionUtils.getFullStackTrace(e));
                throw new SerializationException(e);
            }
        }

        public static KernelCmdlineColumn fromJson(String json) {
            final String nonSafeJson = json == null ? "{}" : json;
            try {
                return new ObjectMapper()
                        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                        .readValue(nonSafeJson, KernelCmdlineColumn.class);
            } catch (JsonProcessingException e) {
                log.error("Cannot deserialize {} because of {}", nonSafeJson, ExceptionUtils.getRootCauseMessage(e));
                log.debug("Cannot deserialize {}. Details {}", nonSafeJson, ExceptionUtils.getFullStackTrace(e));
                throw new SerializationException(e);
            }
        }

        public void toVdsStatic(VdsStatic vdsStatic) {
            vdsStatic.setCurrentKernelCmdline(current);
            vdsStatic.setKernelCmdlineParsable(parsable);
            vdsStatic.setKernelCmdlineBlacklistNouveau(blacklistNouveau);
            vdsStatic.setKernelCmdlineIommu(iommu);
            vdsStatic.setKernelCmdlineKvmNested(kvmNested);
            vdsStatic.setKernelCmdlineUnsafeInterrupts(unsafeInterrupts);
            vdsStatic.setKernelCmdlinePciRealloc(pciRealloc);
            vdsStatic.setKernelCmdlineFips(fips);
            vdsStatic.setKernelCmdlineSmtDisabled(smtDisabled);
        }

        public void toVds(VDS vds) {
            vds.setCurrentKernelCmdline(current);
            vds.setKernelCmdlineParsable(parsable);
            vds.setKernelCmdlineBlacklistNouveau(blacklistNouveau);
            vds.setKernelCmdlineIommu(iommu);
            vds.setKernelCmdlineKvmNested(kvmNested);
            vds.setKernelCmdlineUnsafeInterrupts(unsafeInterrupts);
            vds.setKernelCmdlinePciRealloc(pciRealloc);
            vds.setKernelCmdlineFips(fips);
            vds.setKernelCmdlineSmtDisabled(smtDisabled);
        }

        public static KernelCmdlineColumn fromVdsStatic(VdsStatic vdsStatic) {
            final KernelCmdlineColumn kernelCmdlineColumn = new KernelCmdlineColumn();
            kernelCmdlineColumn.current = vdsStatic.getCurrentKernelCmdline();
            kernelCmdlineColumn.parsable = vdsStatic.isKernelCmdlineParsable();
            kernelCmdlineColumn.blacklistNouveau = vdsStatic.isKernelCmdlineBlacklistNouveau();
            kernelCmdlineColumn.iommu = vdsStatic.isKernelCmdlineIommu();
            kernelCmdlineColumn.kvmNested = vdsStatic.isKernelCmdlineKvmNested();
            kernelCmdlineColumn.unsafeInterrupts = vdsStatic.isKernelCmdlineUnsafeInterrupts();
            kernelCmdlineColumn.pciRealloc = vdsStatic.isKernelCmdlinePciRealloc();
            kernelCmdlineColumn.fips = vdsStatic.isKernelCmdlineFips();
            kernelCmdlineColumn.smtDisabled = vdsStatic.isKernelCmdlineSmtDisabled();
            return kernelCmdlineColumn;
        }
    }
}
