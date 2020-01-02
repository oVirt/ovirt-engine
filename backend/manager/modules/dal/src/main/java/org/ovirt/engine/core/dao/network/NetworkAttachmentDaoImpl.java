package org.ovirt.engine.core.dao.network;

import static org.ovirt.engine.core.dao.network.HostNetworkQosDaoImpl.HostNetworkQosDaoDbFacadaeImplMapper.MAPPER;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
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

    @Inject
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;


    private final RowMapper<HostNetworkQos> qosDaoRetriever = (rs, rowNum) -> hostNetworkQosDao.get(getGuid(rs, "id"));
    private final RowMapper<DnsResolverConfiguration> dnsDaoRetriever = (rs, rowNum) ->
            dnsResolverConfigurationDao.get(getGuid(rs, "dns_resolver_configuration_id")
    );
    private final NetworkAttachmentRowMapper qosDaoAttachmentMapper = new NetworkAttachmentRowMapper(
            qosDaoRetriever, dnsDaoRetriever
    );

    private final RowMapper<HostNetworkQos> qosResultSetMapper = (rs, rowNum) -> {
        HostNetworkQos qos = null;
        if (getGuid(rs, "qos_id") != null) {
            qos = MAPPER.createQosEntity(rs);
            qos.setId(getGuid(rs, "id"));
        }
        return qos;
    };

    private final NetworkAttachmentRowMapper attachmentWithQosMapper = new NetworkAttachmentRowMapper(
            qosResultSetMapper, dnsDaoRetriever
    );

    private static final class NetworkAttachmentRowMapper  implements RowMapper<NetworkAttachment>  {

        private final RowMapper<HostNetworkQos> hostNetworkQosMapper;
        private final RowMapper<DnsResolverConfiguration> dnsMapper;

        NetworkAttachmentRowMapper(RowMapper<HostNetworkQos> qosMapper, RowMapper<DnsResolverConfiguration> dnsMapper) {
            this.hostNetworkQosMapper = qosMapper;
            this.dnsMapper = dnsMapper;
        }

        @Override
        public NetworkAttachment mapRow(ResultSet rs, int rowNum) throws SQLException {
            NetworkAttachment entity = new NetworkAttachment();
            entity.setId(getGuid(rs, "id"));
            entity.setNetworkId(getGuid(rs, "network_id"));
            entity.setNicId(getGuid(rs, "nic_id"));
            entity.setProperties(getCustomProperties(rs));

            final IpConfiguration ipConfiguration = new IpConfiguration();

            final String bootProtocol = rs.getString("boot_protocol");
            if (bootProtocol != null) {
                final IPv4Address iPv4Address = createIpv4Address(rs, bootProtocol);
                ipConfiguration.getIPv4Addresses().add(iPv4Address);
            }

            final String v6BootProtocol = rs.getString("ipv6_boot_protocol");
            if (v6BootProtocol != null) {
                final IpV6Address ipV6Address = createIpV6Address(rs, v6BootProtocol);
                ipConfiguration.getIpV6Addresses().add(ipV6Address);
            }

            if (bootProtocol != null || v6BootProtocol != null) {
                entity.setIpConfiguration(ipConfiguration);
            }
            entity.setHostNetworkQos(asAnonymousHostNetworkQos(hostNetworkQosMapper.mapRow(rs, rowNum)));

            entity.setDnsResolverConfiguration(dnsMapper.mapRow(rs, rowNum));

            return entity;
        }
    }

    public NetworkAttachmentDaoImpl() {
        super("NetworkAttachment");
    }

    @Override
    public List<NetworkAttachment> getAllForNic(Guid nicId) {
        return getCallsHandler().executeReadList("GetNetworkAttachmentsByNicId",
                qosDaoAttachmentMapper,
                getCustomMapSqlParameterSource().addValue("nic_id", nicId));
    }

    @Override
    public List<NetworkAttachment> getAllForNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetNetworkAttachmentsByNetworkId",
                qosDaoAttachmentMapper,
                getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public NetworkAttachment getNetworkAttachmentByNicIdAndNetworkId(Guid nicId, Guid networkId) {
        Objects.requireNonNull(nicId, "nicId cannot be null");
        Objects.requireNonNull(networkId, "networkId cannot be null");

        return getCallsHandler().executeRead("GetNetworkAttachmentWithQosByNicIdAndNetworkId",
                attachmentWithQosMapper,
                getCustomMapSqlParameterSource().addValue("nic_id", nicId).addValue("network_id", networkId));
    }

    @Override
    public List<NetworkAttachment> getAllForHost(Guid hostId) {
        return getCallsHandler().executeReadList("GetNetworkAttachmentsWithQosByHostId",
                attachmentWithQosMapper,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
    }

    @Override
    public void remove(Guid id) {
        hostNetworkQosDao.remove(id);
        dnsResolverConfigurationDao.removeByNetworkAttachmentId(id);
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

        mapIpConfiguration(networkAttachment, mapper);

        mapper.addValue("dns_resolver_configuration_id", getDnsResolverConfigurationId(networkAttachment));
        return mapper;
    }

    private Guid getDnsResolverConfigurationId(NetworkAttachment networkAttachment) {
        DnsResolverConfiguration dnsResolverConfiguration = networkAttachment.getDnsResolverConfiguration();
        if (dnsResolverConfiguration == null) {
            return null;
        }

        return dnsResolverConfiguration.getId();
    }

    private void mapIpConfiguration(NetworkAttachment networkAttachment, MapSqlParameterSource mapper) {
        final IpConfiguration ipConfiguration = networkAttachment.getIpConfiguration() == null
                ? new IpConfiguration()
                : networkAttachment.getIpConfiguration();
        mapIpv4Configuration(mapper, ipConfiguration);
        mapIpv6Configuration(mapper, ipConfiguration);
    }

    private void mapIpv4Configuration(MapSqlParameterSource mapper, IpConfiguration ipConfiguration) {
        if (ipConfiguration.hasIpv4PrimaryAddressSet()) {
            final IPv4Address primaryIpv4Address = ipConfiguration.getIpv4PrimaryAddress();
            mapper.addValue("boot_protocol", EnumUtils.nameOrNull(primaryIpv4Address.getBootProtocol()))
                    .addValue("address", primaryIpv4Address.getAddress())
                    .addValue("netmask", primaryIpv4Address.getNetmask())
                    .addValue("gateway", primaryIpv4Address.getGateway());
        } else {
            mapper.addValue("boot_protocol", null)
                    .addValue("address", null)
                    .addValue("netmask", null)
                    .addValue("gateway", null);
        }
    }

    private void mapIpv6Configuration(MapSqlParameterSource mapper, IpConfiguration ipConfiguration) {
        if (ipConfiguration.hasIpv6PrimaryAddressSet()) {
            final IpV6Address primaryIpv6Address = ipConfiguration.getIpv6PrimaryAddress();
            mapper.addValue("ipv6_boot_protocol", EnumUtils.nameOrNull(primaryIpv6Address.getBootProtocol()))
                    .addValue("ipv6_address", primaryIpv6Address.getAddress())
                    .addValue("ipv6_prefix", primaryIpv6Address.getPrefix())
                    .addValue("ipv6_gateway", primaryIpv6Address.getGateway());
        } else {
            mapper.addValue("ipv6_boot_protocol", null)
                    .addValue("ipv6_address", null)
                    .addValue("ipv6_prefix", null)
                    .addValue("ipv6_gateway", null);
        }
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<NetworkAttachment> createEntityRowMapper() {
        return qosDaoAttachmentMapper;
    }

    @Override
    public void save(NetworkAttachment entity) {
        verifyRelationWithHostNetworkQos(entity);
        hostNetworkQosDao.persistQosChanges(entity.getId(), asHostNetworkQos(entity.getHostNetworkQos()));

        DnsResolverConfiguration dnsResolverConfiguration = entity.getDnsResolverConfiguration();
        if (dnsResolverConfiguration != null) {
            Validate.isTrue(dnsResolverConfiguration.getId() == null);
            dnsResolverConfigurationDao.save(dnsResolverConfiguration);
        }

        super.save(entity);
    }

    private HostNetworkQos asHostNetworkQos(AnonymousHostNetworkQos anonymousHostNetworkQos) {
        return HostNetworkQos.fromAnonymousHostNetworkQos(anonymousHostNetworkQos);
    }

    private static AnonymousHostNetworkQos asAnonymousHostNetworkQos(HostNetworkQos hostNetworkQos) {
        return AnonymousHostNetworkQos.fromHostNetworkQos(hostNetworkQos);
    }

    @Override
    public void update(NetworkAttachment entity) {
        verifyRelationWithHostNetworkQos(entity);
        hostNetworkQosDao.persistQosChanges(entity.getId(), asHostNetworkQos(entity.getHostNetworkQos()));

        DnsResolverConfiguration dnsResolverConfiguration = entity.getDnsResolverConfiguration();
        if (dnsResolverConfiguration == null) {
            dnsResolverConfigurationDao.removeByNetworkAttachmentId(entity.getId());
        } else {
            if (dnsResolverConfiguration.getId() == null) {
                dnsResolverConfigurationDao.save(dnsResolverConfiguration);
            } else {
                dnsResolverConfigurationDao.update(dnsResolverConfiguration);
            }
        }

        super.update(entity);
    }

    private void verifyRelationWithHostNetworkQos(NetworkAttachment entity) {
        AnonymousHostNetworkQos hostNetworkQos = entity.getHostNetworkQos();
        if (hostNetworkQos != null && !Objects.equals(hostNetworkQos.getId(), entity.getId())) {
            throw new IllegalArgumentException(
                String.format("Overridden HostNetworkQos using id %s which does not related to given entity id %s",
                    hostNetworkQos.getId(),
                    entity.getId()));
        }
    }

    private static IPv4Address createIpv4Address(ResultSet rs, String bootProtocol) throws SQLException {
        final IPv4Address iPv4Address = new IPv4Address();
        iPv4Address.setBootProtocol(Ipv4BootProtocol.valueOf(bootProtocol));
        iPv4Address.setAddress(rs.getString("address"));
        iPv4Address.setNetmask(rs.getString("netmask"));
        iPv4Address.setGateway(rs.getString("gateway"));
        return iPv4Address;
    }

    private static IpV6Address createIpV6Address(ResultSet rs, String v6BootProtocol) throws SQLException {
        final IpV6Address ipV6Address = new IpV6Address();
        ipV6Address.setBootProtocol(Ipv6BootProtocol.valueOf(v6BootProtocol));
        ipV6Address.setAddress(rs.getString("ipv6_address"));
        if (rs.getObject("ipv6_prefix") != null) {
            ipV6Address.setPrefix(rs. getInt("ipv6_prefix"));
        }
        ipV6Address.setGateway(rs.getString("ipv6_gateway"));
        return ipV6Address;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getCustomProperties(ResultSet rs) throws SQLException {
        return SerializationFactory.getDeserializer()
                .deserializeOrCreateNew(rs.getString("custom_properties"), LinkedHashMap.class);
    }
}
