package org.ovirt.engine.core.dao.network;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class DnsResolverConfigurationDaoImpl extends DefaultGenericDao<DnsResolverConfiguration, Guid> implements DnsResolverConfigurationDao {

    public DnsResolverConfigurationDaoImpl() {
        super("DnsResolverConfiguration");
    }

    private final RowMapper<DnsResolverConfiguration> rowMapper = (rs, rowNum) -> {
        DnsResolverConfiguration entity = new DnsResolverConfiguration();

        entity.setId(getGuid(rs, "id"));
        entity.setNameServers(getNameServersByDnsResolverConfigurationId(entity.getId()));

        return entity;
    };

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<DnsResolverConfiguration> createEntityRowMapper() {
        return rowMapper;
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(DnsResolverConfiguration dnsResolverConfiguration) {
        MapSqlParameterSource mapper = createIdParameterMapper(dnsResolverConfiguration.getId());

        return mapper;
    }

    private List<NameServer> getNameServersByDnsResolverConfigurationId(Guid dnsResolverConfigurationId) {
        return getCallsHandler().executeReadList("GetNameServersByDnsResolverConfigurationId",
                (rs, rowNum) -> {
                    String address = rs.getString("address");

                    return new NameServer(address);
                },
                getCustomMapSqlParameterSource().addValue("dns_resolver_configuration_id", dnsResolverConfigurationId));
    }

    private void saveNameServersByDnsResolverConfigurationId(Guid dnsResolverConfigurationId, List<NameServer> nameServers) {
        for(int i = 0; i < nameServers.size(); i++) {
            MapSqlParameterSource mapper = getCustomMapSqlParameterSource()
                    .addValue("dns_resolver_configuration_id", dnsResolverConfigurationId)
                    .addValue("address", nameServers.get(i).getAddress())
                    .addValue("position", i);

            getCallsHandler().executeModification("InsertNameServer", mapper);
        }
    }

    private void removeNameServersByDnsResolverConfigurationId(Guid id) {
        getCallsHandler().executeModification("DeleteNameServersByDnsResolverConfigurationId",
                createIdParameterMapper(id));
    }

    @Override
    public void removeByNetworkAttachmentId(Guid id) {
        getCallsHandler().executeModification("DeleteDnsResolverConfigurationByNetworkAttachmentId",
                createIdParameterMapper(id));
    }

    @Override
    public void removeByNetworkId(Guid id) {
        getCallsHandler().executeModification("DeleteDnsResolverConfigurationByNetworkId",
                createIdParameterMapper(id));
    }

    @Override
    public void removeByVdsDynamicId(Guid id) {
        getCallsHandler().executeModification("DeleteDnsResolverConfigurationByVdsDynamicId",
                createIdParameterMapper(id));
    }

    @Override
    public void save(DnsResolverConfiguration entity) {
        if (entity.getId() == null) {
            entity.setId(Guid.newGuid());
        }
        super.save(entity);
        saveNameServersByDnsResolverConfigurationId(entity.getId(), entity.getNameServers());
    }

    @Override
    public void remove(Guid id) {
        removeNameServersByDnsResolverConfigurationId(id);
        super.remove(id);
    }

    @Override
    public void update(DnsResolverConfiguration entity) {
        super.update(entity);
        removeNameServersByDnsResolverConfigurationId(entity.getId());
        saveNameServersByDnsResolverConfigurationId(entity.getId(), entity.getNameServers());
    }
}
