package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class NetworkAttachmentDaoImpl extends DefaultGenericDao<NetworkAttachment, Guid> implements NetworkAttachmentDao {

    @Inject
    private HostNetworkQosDao hostNetworkQosDao;

    private NetworkAttachmentRowMapper networkAttachmentRowMapper = new NetworkAttachmentRowMapper();

    public NetworkAttachmentDaoImpl() {
        super("NetworkAttachment");
    }

    @Override
    public List<NetworkAttachment> getAllForNic(Guid nicId) {
        return getCallsHandler().executeReadList("GetNetworkAttachmentsByNicId",
                networkAttachmentRowMapper,
                getCustomMapSqlParameterSource().addValue("nic_id", nicId));
    }

    @Override
    public List<NetworkAttachment> getAllForNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetNetworkAttachmentsByNetworkId",
                networkAttachmentRowMapper,
                getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public NetworkAttachment getNetworkAttachmentByNicIdAndNetworkId(Guid nicId, Guid networkId) {
        Objects.requireNonNull(nicId, "nicId cannot be null");
        Objects.requireNonNull(networkId, "networkId cannot be null");

        return getCallsHandler().executeRead("GetNetworkAttachmentByNicIdAndNetworkId",
                networkAttachmentRowMapper,
                getCustomMapSqlParameterSource().addValue("nic_id", nicId).addValue("network_id", networkId));
    }

    @Override
    public List<NetworkAttachment> getAllForHost(Guid hostId) {
        return getCallsHandler().executeReadList("GetNetworkAttachmentsByHostId",
                networkAttachmentRowMapper,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
    }

    @Override
    public void remove(Guid id) {
        hostNetworkQosDao.remove(id);
        super.remove(id);
    }

    @Override
    public void removeByNetworkId(Guid networkId) {
        List<NetworkAttachment> networkAttachments = getAllForNetwork(networkId);
        for (NetworkAttachment networkAttachment : networkAttachments) {
            hostNetworkQosDao.remove(networkAttachment.getId());
        }

        getCallsHandler().executeModification("RemoveNetworkAttachmentByNetworkId", createIdParameterMapper(networkId));
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
        boolean hasPrimaryAddressSet = ipConfiguration.hasPrimaryAddressSet();
        IPv4Address primaryAddress = hasPrimaryAddressSet ? ipConfiguration.getPrimaryAddress() : null;

        mapper.addValue("boot_protocol",
                hasPrimaryAddressSet ? EnumUtils.nameOrNull(primaryAddress.getBootProtocol()) : null)
                .addValue("address", hasPrimaryAddressSet ? primaryAddress.getAddress() : null)
                .addValue("netmask", hasPrimaryAddressSet ? primaryAddress.getNetmask() : null)
                .addValue("gateway", hasPrimaryAddressSet ? primaryAddress.getGateway() : null);
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<NetworkAttachment> createEntityRowMapper() {
        return networkAttachmentRowMapper;
    }

    @Override
    public void save(NetworkAttachment entity) {
        verifyRelationWithHostNetworkQos(entity);
        verifyUnsetStoragePoolIdAndNameOnQos(entity);
        persistQosChanges(entity);
        super.save(entity);
    }

    @Override
    public void update(NetworkAttachment entity) {
        verifyRelationWithHostNetworkQos(entity);
        verifyUnsetStoragePoolIdAndNameOnQos(entity);
        persistQosChanges(entity);
        super.update(entity);
    }

    private void persistQosChanges(NetworkAttachment attachment) {
        Guid id = attachment.getId();
        HostNetworkQos oldQos = hostNetworkQosDao.get(id);
        HostNetworkQos qos = attachment.getHostNetworkQos();
        if (qos == null) {
            if (oldQos != null) {
                hostNetworkQosDao.remove(id);
            }
        } else {
            qos.setId(id);
            if (oldQos == null) {
                hostNetworkQosDao.save(qos);
            } else if (!qos.equals(oldQos)) {
                hostNetworkQosDao.update(qos);
            }
        }
    }

    private void verifyUnsetStoragePoolIdAndNameOnQos(NetworkAttachment entity) {
        HostNetworkQos hostNetworkQos = entity.getHostNetworkQos();
        if ((hostNetworkQos != null) && (hostNetworkQos.getStoragePoolId() != null || hostNetworkQos.getName() != null)) {
            throw new IllegalArgumentException("When persisting overriding qos instance, there must not be storagePoolId nor name set.");
        }
    }

    private void verifyRelationWithHostNetworkQos(NetworkAttachment entity) {
        HostNetworkQos hostNetworkQos = entity.getHostNetworkQos();
        if (hostNetworkQos != null && !Objects.equals(hostNetworkQos.getId(), entity.getId())) {
            throw new IllegalArgumentException(
                String.format("Overridden HostNetworkQos using id %s which does not related to given entity id %s",
                    hostNetworkQos.getId(),
                    entity.getId()));
        }
    }

    private class NetworkAttachmentRowMapper implements RowMapper<NetworkAttachment> {

        @Override
        public NetworkAttachment mapRow(ResultSet rs, int rowNum) throws SQLException {
            NetworkAttachment entity = new NetworkAttachment();
            entity.setId(getGuid(rs, "id"));
            entity.setNetworkId(getGuid(rs, "network_id"));
            entity.setNicId(getGuid(rs, "nic_id"));
            entity.setProperties(getCustomProperties(rs));

            IpConfiguration ipConfiguration = new IpConfiguration();
            String bootProtocol = rs.getString("boot_protocol");
            if (bootProtocol != null) {
                ipConfiguration.setIPv4Addresses(new ArrayList<>());
                IPv4Address iPv4Address = new IPv4Address();
                iPv4Address.setBootProtocol(NetworkBootProtocol.valueOf(bootProtocol));
                iPv4Address.setAddress(rs.getString("address"));
                iPv4Address.setNetmask(rs.getString("netmask"));
                iPv4Address.setGateway(rs.getString("gateway"));
                ipConfiguration.getIPv4Addresses().add(iPv4Address);
                entity.setIpConfiguration(ipConfiguration);
            }

            entity.setHostNetworkQos(hostNetworkQosDao.get(entity.getId()));

            return entity;
        }

        @SuppressWarnings("unchecked")
        private Map<String, String> getCustomProperties(ResultSet rs) throws SQLException {
            return SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("custom_properties"), LinkedHashMap.class);
        }
    }
}
