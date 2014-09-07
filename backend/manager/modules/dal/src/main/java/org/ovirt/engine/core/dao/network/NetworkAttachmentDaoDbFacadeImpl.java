package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class NetworkAttachmentDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<NetworkAttachment, Guid> implements NetworkAttachmentDao {

    public NetworkAttachmentDaoDbFacadeImpl() {
        super("NetworkAttachment");
    }

    @Override
    public List<NetworkAttachment> getAllForNic(Guid nicId) {
        return getCallsHandler().executeReadList("GetNetworkAttachmentsByNicId",
                NetworkAttachmentRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("nic_id", nicId));
    }

    @Override
    public List<NetworkAttachment> getAllForHost(Guid hostId) {
        return getCallsHandler().executeReadList("GetNetworkAttachmentsByHostId",
                NetworkAttachmentRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(NetworkAttachment networkAttachment) {
        MapSqlParameterSource mapper = createIdParameterMapper(networkAttachment.getId())
                .addValue("network_id", networkAttachment.getNetworkId())
                .addValue("nic_id", networkAttachment.getNicId())
                .addValue("custom_properties",
                        SerializationFactory.getSerializer().serialize(networkAttachment.getProperties()));

        IpConfiguration ipConfiguration = networkAttachment.getIpConfiguration();
        mapIpConfiguration(mapper, ipConfiguration == null ? new IpConfiguration() : ipConfiguration);

        return mapper;
    }

    private void mapIpConfiguration(MapSqlParameterSource mapper, IpConfiguration ipConfiguration) {
        mapper.addValue("boot_protocol", EnumUtils.nameOrNull(ipConfiguration.getBootProtocol()))
                .addValue("address", ipConfiguration.getAddress())
                .addValue("netmask", ipConfiguration.getNetmask())
                .addValue("gateway", ipConfiguration.getGateway());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<NetworkAttachment> createEntityRowMapper() {
        return NetworkAttachmentRowMapper.INSTANCE;
    }

    private static class NetworkAttachmentRowMapper implements RowMapper<NetworkAttachment> {

        public static final NetworkAttachmentRowMapper INSTANCE = new NetworkAttachmentRowMapper();

        @SuppressWarnings("unchecked")
        @Override
        public NetworkAttachment mapRow(ResultSet rs, int rowNum) throws SQLException {
            NetworkAttachment entity = new NetworkAttachment();
            entity.setId(getGuid(rs, "id"));
            entity.setNetworkId(getGuid(rs, "network_id"));
            entity.setNicId(getGuid(rs, "nic_id"));
            entity.setProperties(SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("custom_properties"), LinkedHashMap.class));

            IpConfiguration ipConfiguration = new IpConfiguration();
            String bootProtocol = rs.getString("boot_protocol");
            if (bootProtocol != null) {
                ipConfiguration.setBootProtocol(NetworkBootProtocol.valueOf(bootProtocol));
                ipConfiguration.setAddress(rs.getString("address"));
                ipConfiguration.setNetmask(rs.getString("netmask"));
                ipConfiguration.setGateway(rs.getString("gateway"));
                entity.setIpConfiguration(ipConfiguration);
            }

            return entity;
        }
    }
}
