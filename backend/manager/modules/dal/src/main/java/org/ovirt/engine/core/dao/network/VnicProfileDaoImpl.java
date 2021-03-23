package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VnicProfileDaoImpl extends DefaultGenericDao<VnicProfile, Guid> implements VnicProfileDao {

    public VnicProfileDaoImpl() {
        super("VnicProfile");
    }

    @Override
    public List<VnicProfile> getAllForNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVnicProfilesByNetworkId",
                VnicProfileRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public List<VnicProfile> getAllByFailoverVnicProfileId(Guid failoverId) {
        return getCallsHandler().executeReadList("GetVnicProfilesByFailoverVnicProfileId",
                VnicProfileRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("failover_vnic_profile_id", failoverId));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VnicProfile profile) {
        return createIdParameterMapper(profile.getId())
                .addValue("name", profile.getName())
                .addValue("network_id", profile.getNetworkId())
                .addValue("network_qos_id", profile.getNetworkQosId())
                .addValue("port_mirroring", profile.isPortMirroring())
                .addValue("passthrough", profile.isPassthrough())
                .addValue("migratable", profile.isMigratable())
                .addValue("description", profile.getDescription())
                .addValue("custom_properties",
                        SerializationFactory.getSerializer().serialize(profile.getCustomProperties()))
                .addValue("network_filter_id", profile.getNetworkFilterId())
                .addValue("failover_vnic_profile_id", profile.getFailoverVnicProfileId());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<VnicProfile> createEntityRowMapper() {
        return VnicProfileRowMapper.INSTANCE;
    }

    abstract static class VnicProfileRowMapperBase<T extends VnicProfile> implements RowMapper<T> {

        @Override
        @SuppressWarnings("unchecked")
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T entity = createVnicProfileEntity();
            entity.setId(getGuid(rs, "id"));
            entity.setName(rs.getString("name"));
            entity.setNetworkId(getGuid(rs, "network_id"));
            entity.setNetworkQosId(getGuid(rs, "network_qos_id"));
            entity.setCustomProperties(SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("custom_properties"), LinkedHashMap.class));
            entity.setPortMirroring(rs.getBoolean("port_mirroring"));
            entity.setPassthrough(rs.getBoolean("passthrough"));
            entity.setMigratable(rs.getBoolean("migratable"));
            entity.setDescription(rs.getString("description"));
            entity.setNetworkFilterId(getGuid(rs, "network_filter_id"));
            entity.setFailoverVnicProfileId(getGuid(rs, "failover_vnic_profile_id"));
            return entity;
        }

        protected abstract T createVnicProfileEntity();
    }

    private static class VnicProfileRowMapper extends VnicProfileRowMapperBase<VnicProfile> {

        public static final VnicProfileRowMapper INSTANCE = new VnicProfileRowMapper();

        @Override
        protected VnicProfile createVnicProfileEntity() {
            return new VnicProfile();
        }
    }
}
