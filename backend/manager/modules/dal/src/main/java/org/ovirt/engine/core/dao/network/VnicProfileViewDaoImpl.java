package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultReadDao;
import org.ovirt.engine.core.dao.VersionRowMapper;
import org.ovirt.engine.core.dao.network.VnicProfileDaoImpl.VnicProfileRowMapperBase;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VnicProfileViewDaoImpl extends DefaultReadDao<VnicProfileView, Guid> implements VnicProfileViewDao {

    public VnicProfileViewDaoImpl() {
        super("VnicProfileView");

    }

    @Override
    public VnicProfileView get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public VnicProfileView get(Guid id, Guid userId, boolean filtered) {
        return getCallsHandler().executeRead(getProcedureNameForGet(),
                VnicProfileViewRowMapper.INSTANCE,
                createIdParameterMapper(id).addValue("user_id", userId).addValue("is_filtered", filtered));
    }

    @Override
    public List<VnicProfileView> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<VnicProfileView> getAll(Guid userId, boolean filtered) {
        return getCallsHandler().executeReadList(getProcedureNameForGetAll(),
                VnicProfileViewRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("user_id", userId).addValue("is_filtered", filtered));
    }

    @Override
    public List<VnicProfileView> getAllForNetwork(Guid networkId) {
        return getAllForNetwork(networkId, null, false);
    }

    @Override
    public List<VnicProfileView> getAllForNetwork(Guid networkId, Guid userId, boolean filtered) {
        return getCallsHandler().executeReadList("GetVnicProfileViewsByNetworkId",
                VnicProfileViewRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("network_id", networkId)
                        .addValue("user_id", userId)
                        .addValue("is_filtered", filtered));
    }

    @Override
    public List<VnicProfileView> getAllForDataCenter(Guid id) {
        return getAllForDataCenter(id, null, false);
    }

    @Override
    public List<VnicProfileView> getAllForDataCenter(Guid id, Guid userId, boolean filtered) {
        return getCallsHandler().executeReadList("GetVnicProfileViewsByDataCenterId",
                VnicProfileViewRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("id", id)
                        .addValue("user_id", userId)
                        .addValue("is_filtered", filtered));
    }

    @Override
    public List<VnicProfileView> getAllForCluster(Guid id) {
        return getAllForCluster(id, null, false);
    }

    @Override
    public List<VnicProfileView> getAllForCluster(Guid id, Guid userId, boolean filtered) {
        return getCallsHandler().executeReadList("GetVnicProfileViewsByClusterId",
                VnicProfileViewRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("id", id)
                        .addValue("user_id", userId)
                        .addValue("is_filtered", filtered));
    }

    @Override
    protected VnicProfileViewRowMapper createEntityRowMapper() {
        return VnicProfileViewRowMapper.INSTANCE;
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    private static class VnicProfileViewRowMapper extends VnicProfileRowMapperBase<VnicProfileView> {

        public static final VnicProfileViewRowMapper INSTANCE = new VnicProfileViewRowMapper();

        @Override
        public VnicProfileView mapRow(ResultSet rs, int rowNum) throws SQLException {
            VnicProfileView entity = super.mapRow(rs, rowNum);
            entity.setNetworkName(rs.getString("network_name"));
            entity.setNetworkQosName(rs.getString("network_qos_name"));
            entity.setDataCenterName(rs.getString("data_center_name"));
            entity.setCompatibilityVersion(new VersionRowMapper("compatibility_version").mapRow(rs, rowNum));
            entity.setNetworkFilterName(rs.getString("network_filter_name"));
            entity.setDataCenterId(getGuid(rs, "data_center_id"));
            entity.setFailoverVnicProfileName(rs.getString("failover_vnic_profile_name"));
            return entity;
        }

        @Override
        protected VnicProfileView createVnicProfileEntity() {
            return new VnicProfileView();
        }
    }

    @Override
    public List<VnicProfileView> getAllForNetworkQos(Guid qosId) {
        return getCallsHandler().executeReadList("GetVnicProfileViewsByNetworkQosId",
                VnicProfileViewRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("network_qos_id", qosId));
    }

    @Override
    public List<VnicProfileView> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, VnicProfileViewRowMapper.INSTANCE);
    }

}
