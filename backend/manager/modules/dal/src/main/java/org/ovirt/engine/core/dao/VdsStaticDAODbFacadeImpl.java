package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@link VdsDAO} that uses previously written code from
 * {@code DbFacade}.
 */
@Named
@Singleton
public class VdsStaticDAODbFacadeImpl extends BaseDAODbFacade implements VdsStaticDAO {

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
    public List<VdsStatic> getAllForVdsGroup(Guid vdsGroup) {
        return getCallsHandler().executeReadList("GetVdsStaticByVdsGroupId",
                VdsStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", vdsGroup));
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
                .addValue("vds_group_id", vds.getVdsGroupId())
                .addValue("vds_id", vds.getId())
                .addValue("vds_name", vds.getName())
                .addValue("server_SSL_enabled", vds.isServerSslEnabled())
                .addValue("vds_type", vds.getVdsType())
                .addValue("vds_strength", vds.getVdsStrength())
                .addValue("pm_enabled", vds.isPmEnabled())
                .addValue("pm_proxy_preferences", vds.getPmProxyPreferences())
                .addValue("pm_detect_kdump", vds.isPmKdumpDetection())
                .addValue("otp_validity", vds.getOtpValidity())
                .addValue("vds_spm_priority", vds.getVdsSpmPriority())
                .addValue("console_address", vds.getConsoleAddress())
                .addValue("sshKeyFingerprint", vds.getSshKeyFingerprint())
                .addValue("ssh_port", vds.getSshPort())
                .addValue("ssh_username", vds.getSshUsername())
                .addValue("disable_auto_pm", vds.isDisablePowerManagementPolicy())
                .addValue("host_provider_id", vds.getHostProviderId());
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        getCallsHandler().executeModification("DeleteVdsStatic", parameterSource);
    }

    @Override
    public List<VdsStatic> getAll() {
        throw new NotImplementedException();
    }

    private final static class VdsStaticRowMapper implements RowMapper<VdsStatic> {
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
            entity.setVdsGroupId(getGuidDefaultEmpty(rs, "vds_group_id"));
            entity.setId(getGuidDefaultEmpty(rs, "vds_id"));
            entity.setSshPort(rs.getInt("ssh_port"));
            entity.setSshUsername(rs.getString("ssh_username"));
            entity.setVdsGroupId(Guid.createGuidFromStringDefaultEmpty(rs
                    .getString("vds_group_id")));
            entity.setId(Guid.createGuidFromStringDefaultEmpty(rs
                    .getString("vds_id")));
            entity.setName(rs.getString("vds_name"));
            entity.setServerSslEnabled(rs
                    .getBoolean("server_SSL_enabled"));
            entity.setVdsType(VDSType.forValue(rs.getInt("vds_type")));
            entity.setVdsStrength(rs.getInt("vds_strength"));
            entity.setPmEnabled(rs.getBoolean("pm_enabled"));
            entity.setPmProxyPreferences(rs.getString("pm_proxy_preferences"));
            entity.setPmKdumpDetection(rs.getBoolean("pm_detect_kdump"));
            entity.setOtpValidity(rs.getLong("otp_validity"));
            entity.setSshKeyFingerprint(rs.getString("sshKeyFingerprint"));
            entity.setConsoleAddress(rs.getString("console_address"));
            entity.setDisablePowerManagementPolicy(rs.getBoolean("disable_auto_pm"));
            entity.setHostProviderId(getGuid(rs, "host_provider_id"));

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

}
