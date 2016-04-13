package org.ovirt.engine.core.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * <code>VdsDaoImpl</code> provides an implementation of {@link VdsDao} that uses previously written code from
 * {@code DbFacade}.
 */
@Named
@Singleton
public class VdsStaticDaoImpl extends BaseDao implements VdsStaticDao {

    @Override
    public VdsStatic get(Guid id) {
        return getCallsHandler().executeRead("GetVdsStaticByVdsId",
                VdsStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
    }

    @Override
    public VdsStatic getByHostName(String host) {
        return getCallsHandler().executeRead("GetVdsStaticByHostName",
                VdsStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("host_name", host));
    }

    @Override
    public List<VdsStatic> getAllWithIpAddress(String address) {
        return getCallsHandler().executeReadList("GetVdsStaticByIp",
                VdsStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("ip", address));
    }

    @Override
    public List<VdsStatic> getAllForCluster(Guid cluster) {
        return getCallsHandler().executeReadList("GetVdsStaticByClusterId",
                VdsStaticRowMapper.instance,
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
                .addValue("protocol", vds.getProtocol())
                .addValue("cluster_id", vds.getClusterId())
                .addValue("vds_id", vds.getId())
                .addValue("vds_name", vds.getName())
                .addValue("server_SSL_enabled", vds.isServerSslEnabled())
                .addValue("vds_type", vds.getVdsType())
                .addValue("vds_strength", vds.getVdsStrength())
                .addValue("pm_enabled", vds.isPmEnabled())
                .addValue("pm_proxy_preferences", FenceProxySourceTypeHelper.saveAsString(vds.getFenceProxySources()))
                .addValue("pm_detect_kdump", vds.isPmKdumpDetection())
                .addValue("otp_validity", vds.getOtpValidity())
                .addValue("vds_spm_priority", vds.getVdsSpmPriority())
                .addValue("console_address", vds.getConsoleAddress())
                .addValue("sshKeyFingerprint", vds.getSshKeyFingerprint())
                .addValue("ssh_port", vds.getSshPort())
                .addValue("ssh_username", vds.getSshUsername())
                .addValue("disable_auto_pm", vds.isDisablePowerManagementPolicy())
                .addValue("host_provider_id", vds.getHostProviderId())
                .addValue("openstack_network_provider_id", vds.getOpenstackNetworkProviderId())
                .addValue("kernel_cmdline", KernelCmdlineColumn.fromVdsStatic(vds).toJson())
                .addValue("last_stored_kernel_cmdline", vds.getLastStoredKernelCmdline());
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

    private static final class VdsStaticRowMapper implements RowMapper<VdsStatic> {
        public static final VdsStaticRowMapper instance = new VdsStaticRowMapper();

        @Override
        public VdsStatic mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            VdsStatic entity = new VdsStatic();
            entity.setHostName(rs.getString("host_name"));
            entity.setComment(rs.getString("free_text_comment"));
            entity.setUniqueID(rs.getString("vds_unique_id"));
            entity.setPort(rs.getInt("port"));
            entity.setProtocol(VdsProtocol.fromValue(rs.getInt("protocol")));
            entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
            entity.setId(getGuidDefaultEmpty(rs, "vds_id"));
            entity.setSshPort(rs.getInt("ssh_port"));
            entity.setSshUsername(rs.getString("ssh_username"));
            entity.setClusterId(Guid.createGuidFromStringDefaultEmpty(rs
                    .getString("cluster_id")));
            entity.setId(Guid.createGuidFromStringDefaultEmpty(rs
                    .getString("vds_id")));
            entity.setName(rs.getString("vds_name"));
            entity.setServerSslEnabled(rs
                    .getBoolean("server_SSL_enabled"));
            entity.setVdsType(VDSType.forValue(rs.getInt("vds_type")));
            entity.setVdsStrength(rs.getInt("vds_strength"));
            entity.setPmEnabled(rs.getBoolean("pm_enabled"));
            entity.setFenceProxySources(
                    FenceProxySourceTypeHelper.parseFromString(rs.getString("pm_proxy_preferences")));
            entity.setPmKdumpDetection(rs.getBoolean("pm_detect_kdump"));
            entity.setOtpValidity(rs.getLong("otp_validity"));
            entity.setSshKeyFingerprint(rs.getString("sshKeyFingerprint"));
            entity.setConsoleAddress(rs.getString("console_address"));
            entity.setDisablePowerManagementPolicy(rs.getBoolean("disable_auto_pm"));
            entity.setHostProviderId(getGuid(rs, "host_provider_id"));
            entity.setOpenstackNetworkProviderId(getGuid(rs, "openstack_network_provider_id"));
            KernelCmdlineColumn.fromJson(rs.getString("kernel_cmdline")).toVdsStatic(entity);
            entity.setLastStoredKernelCmdline(rs.getString("last_stored_kernel_cmdline"));
            return entity;
        }
    }

    @Override
    public VdsStatic getByVdsName(String vdsName) {
        return getCallsHandler().executeRead("GetVdsStaticByVdsName",
                VdsStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("host_name", vdsName));
    }

    @Override
    public List<String> getAllHostNamesPinnedToVm(Guid vm) {
        return getCallsHandler().executeReadList("GetNamesOfHostsDedicatedToVm", getStringMapper(),
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

    /**
     * Model of JSON structured column "kernel_cmdline"
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class KernelCmdlineColumn {

        private String current;
        private boolean parsable;
        private boolean iommu;
        private boolean kvmNested;
        private boolean unsafeInterrupts;
        private boolean pciRealloc;

        public String toJson() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Error during JSON serialization of " + KernelCmdlineColumn.class.getCanonicalName(), ex);
            }
        }

        public static KernelCmdlineColumn fromJson(String json) {
            final String nonSafeJson = json == null ? "{}" : json;
            try {
                return new ObjectMapper().readValue(nonSafeJson, KernelCmdlineColumn.class);
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Error during JSON deserialization of " + KernelCmdlineColumn.class.getCanonicalName(), ex);
            }
        }

        public void toVdsStatic(VdsStatic vdsStatic) {
            vdsStatic.setCurrentKernelCmdline(current);
            vdsStatic.setKernelCmdlineParsable(parsable);
            vdsStatic.setKernelCmdlineIommu(iommu);
            vdsStatic.setKernelCmdlineKvmNested(kvmNested);
            vdsStatic.setKernelCmdlineUnsafeInterrupts(unsafeInterrupts);
            vdsStatic.setKernelCmdlinePciRealloc(pciRealloc);
        }

        public void toVds(VDS vds) {
            vds.setCurrentKernelCmdline(current);
            vds.setKernelCmdlineParsable(parsable);
            vds.setKernelCmdlineIommu(iommu);
            vds.setKernelCmdlineKvmNested(kvmNested);
            vds.setKernelCmdlineUnsafeInterrupts(unsafeInterrupts);
            vds.setKernelCmdlinePciRealloc(pciRealloc);
        }

        public static KernelCmdlineColumn fromVdsStatic(VdsStatic vdsStatic) {
            final KernelCmdlineColumn kernelCmdlineColumn = new KernelCmdlineColumn();
            kernelCmdlineColumn.current = vdsStatic.getCurrentKernelCmdline();
            kernelCmdlineColumn.parsable = vdsStatic.isKernelCmdlineParsable();
            kernelCmdlineColumn.iommu = vdsStatic.isKernelCmdlineIommu();
            kernelCmdlineColumn.kvmNested = vdsStatic.isKernelCmdlineKvmNested();
            kernelCmdlineColumn.unsafeInterrupts = vdsStatic.isKernelCmdlineUnsafeInterrupts();
            kernelCmdlineColumn.pciRealloc = vdsStatic.isKernelCmdlinePciRealloc();
            return kernelCmdlineColumn;
        }
    }
}
