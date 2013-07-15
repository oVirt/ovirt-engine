package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class VnicProfileDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<VnicProfile, Guid> implements VnicProfileDao {

    public VnicProfileDaoDbFacadeImpl() {
        super("VnicProfile");
    }

    @Override
    public List<VnicProfile> getAllForNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVnicProfilesByNetworkId",
                VnicProfileRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VnicProfile profile) {
        return createIdParameterMapper(profile.getId())
                .addValue("name", profile.getName())
                .addValue("network_id", profile.getNetworkId())
                .addValue("port_mirroring", profile.isPortMirroring())
                .addValue("description", profile.getDescription())
                .addValue("custom_properties",
                        SerializationFactory.getSerializer().serialize(profile.getCustomProperties()));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<VnicProfile> createEntityRowMapper() {
        return VnicProfileRowMapper.INSTANCE;
    }

    static class VnicProfileRowMapper implements RowMapper<VnicProfile> {

        public final static VnicProfileRowMapper INSTANCE = new VnicProfileRowMapper();

        @Override
        @SuppressWarnings("unchecked")
        public VnicProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
            VnicProfile entity = new VnicProfile();
            entity.setId(Guid.createGuidFromString(rs.getString("id")));
            entity.setName(rs.getString("name"));
            entity.setNetworkId(Guid.createGuidFromString(rs.getString("network_id")));
            entity.setCustomProperties(SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("custom_properties"), LinkedHashMap.class));
            entity.setPortMirroring(rs.getBoolean("port_mirroring"));
            entity.setDescription(rs.getString("description"));
            return entity;
        }
    }
}
