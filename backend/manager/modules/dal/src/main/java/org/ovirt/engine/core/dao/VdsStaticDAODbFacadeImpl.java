package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@link VdsDAO} that uses previously written code from
 * {@code DbFacade}.
 */
public class VdsStaticDAODbFacadeImpl extends BaseDAODbFacade implements VdsStaticDAO {

    @Override
    public VdsStatic get(Guid id) {
        return getCallsHandler().executeRead("GetVdsStaticByVdsId",
                new VdsStaticRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
    }

    @Override
    public VdsStatic get(String name) {
        return (VdsStatic) DbFacadeUtils.asSingleResult(getCallsHandler().executeReadList("GetVdsStaticByVdsName",
                new VdsStaticRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_name", name)));
    }

    @Override
    public List<VdsStatic> getAllForHost(String host) {
        return getCallsHandler().executeReadList("GetVdsStaticByHostName",
                new VdsStaticRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("host_name", host));
    }

    @Override
    public List<VdsStatic> getAllWithIpAddress(String address) {
        return getCallsHandler().executeReadList("GetVdsStaticByIp",
                new VdsStaticRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("ip", address));
    }

    @Override
    public List<VdsStatic> getAllForVdsGroup(Guid vdsGroup) {
        return getCallsHandler().executeReadList("GetVdsStaticByVdsGroupId",
                new VdsStaticRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", vdsGroup));
    }

    @Override
    public void save(VdsStatic vds) {
        Guid id = vds.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.NewGuid();
            vds.setId(id);
        }
        new SimpleJdbcCall(jdbcTemplate).withProcedureName("InsertVdsStatic").execute(getInsertOrUpdateParams(vds));
    }

    @Override
    public void update(VdsStatic vds) {
        getCallsHandler().executeModification("UpdateVdsStatic", getInsertOrUpdateParams(vds));
    }

    private MapSqlParameterSource getInsertOrUpdateParams(final VdsStatic vds) {
        return getCustomMapSqlParameterSource()
                .addValue("host_name", vds.getHostName())
                .addValue("ip", vds.getManagmentIp())
                .addValue("vds_unique_id", vds.getUniqueID())
                .addValue("port", vds.getPort())
                .addValue("vds_group_id", vds.getVdsGroupId())
                .addValue("vds_id", vds.getId())
                .addValue("vds_name", vds.getVdsName())
                .addValue("server_SSL_enabled", vds.isServerSslEnabled())
                .addValue("vds_type", vds.getVdsType())
                .addValue("vds_strength", vds.getVdsStrength())
                .addValue("pm_type", vds.getPmType())
                .addValue("pm_user", vds.getPmUser())
                .addValue("pm_password", DbFacadeUtils.encryptPassword(vds.getPmPassword()))
                .addValue("pm_port", vds.getPmPort())
                .addValue("pm_options", vds.getPmOptions())
                .addValue("pm_enabled", vds.isPmEnabled())
                .addValue("pm_proxy_preferences", vds.getPmProxyPreferences())
                .addValue("pm_secondary_ip", vds.getPmSecondaryIp())
                .addValue("pm_secondary_type", vds.getPmSecondaryType())
                .addValue("pm_secondary_user", vds.getPmSecondaryUser())
                .addValue("pm_secondary_password", DbFacadeUtils.encryptPassword(vds.getPmSecondaryPassword()))
                .addValue("pm_secondary_port", vds.getPmSecondaryPort())
                .addValue("pm_secondary_options", vds.getPmSecondaryOptions())
                .addValue("pm_secondary_concurrent", vds.isPmSecondaryConcurrent())
                .addValue("otp_validity", vds.getOtpValidity())
                .addValue("vds_spm_priority", vds.getVdsSpmPriority())
                .addValue("console_address", vds.getConsoleAddress())
                .addValue("sshKeyFingerprint", vds.getSSHKeyFingerprint());
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

    private final static class VdsStaticRowMapper implements ParameterizedRowMapper<VdsStatic> {
        @Override
        public VdsStatic mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            VdsStatic entity = new VdsStatic();
            entity.setHostName(rs.getString("host_name"));
            entity.setManagmentIp(rs.getString("ip"));
            entity.setUniqueID(rs.getString("vds_unique_id"));
            entity.setPort(rs.getInt("port"));
            entity.setVdsGroupId(Guid.createGuidFromString(rs
                    .getString("vds_group_id")));
            entity.setId(Guid.createGuidFromString(rs
                    .getString("vds_id")));
            entity.setVdsName(rs.getString("vds_name"));
            entity.setServerSslEnabled(rs
                    .getBoolean("server_SSL_enabled"));
            entity.setVdsType(VDSType.forValue(rs.getInt("vds_type")));
            entity.setVdsStrength(rs.getInt("vds_strength"));
            entity.setPmType(rs.getString("pm_type"));
            entity.setPmUser(rs.getString("pm_user"));
            entity.setPmPassword(DbFacadeUtils.decryptPassword(rs.getString("pm_password")));
            entity.setPmPort((Integer) rs.getObject("pm_port"));
            entity.setPmOptions(rs.getString("pm_options"));
            entity.setPmEnabled(rs.getBoolean("pm_enabled"));
            entity.setPmProxyPreferences(rs.getString("pm_proxy_preferences"));
            entity.setPmSecondaryIp((rs.getString("pm_secondary_ip")));
            entity.setPmSecondaryType(rs.getString("pm_secondary_type"));
            entity.setPmSecondaryUser(rs.getString("pm_secondary_user"));
            entity.setPmSecondaryPassword(DbFacadeUtils.decryptPassword(rs.getString("pm_secondary_password")));
            entity.setPmSecondaryPort((Integer) rs.getObject("pm_secondary_port"));
            entity.setPmSecondaryOptions(rs.getString("pm_secondary_options"));
            entity.setPmSecondaryConcurrent(rs.getBoolean("pm_secondary_concurrent"));
            entity.setOtpValidity(rs.getLong("otp_validity"));
            entity.setSSHKeyFingerprint(rs.getString("sshKeyFingerprint"));
            entity.setConsoleAddress(rs.getString("console_address"));

            return entity;
        }
    }

}
