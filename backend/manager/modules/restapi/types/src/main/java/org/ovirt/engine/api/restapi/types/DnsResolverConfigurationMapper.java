package org.ovirt.engine.api.restapi.types;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NameServer;

public class DnsResolverConfigurationMapper {
    public static org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration map(
            org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration dnsResolverConfiguration,
            DnsResolverConfiguration model) {

        org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration result =
                new org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration();

        if (model.isSetNameServers()) {
            DnsResolverConfiguration.NameServersList nameServersList = model.getNameServers();
            if (nameServersList.isSetNameServers()) {
                List<String> nameServers = nameServersList.getNameServers();

                result.setNameServers(mapNameServers(nameServers));
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static List<NameServer> mapNameServers(List<String> nameServers) {
        return nameServers.stream()
                .filter(StringUtils::isNotEmpty)
                .map(NameServer::new)
                .collect(toList());
    }

    public static DnsResolverConfiguration map(
            org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration entity) {

        if (entity == null) {
            return null;
        }

        List<NameServer> nameServers = entity.getNameServers();
        if (nameServers == null) {
            return null;
        }

        List<String> nameServerAddresses = nameServers
                .stream()
                .map(NameServer::getAddress)
                .collect(toList());

        DnsResolverConfiguration dnsResolverConfiguration = new DnsResolverConfiguration();
        dnsResolverConfiguration.setNameServers(mapAddressesToNameServerList(nameServerAddresses));

        return dnsResolverConfiguration;
    }

    private static DnsResolverConfiguration.NameServersList mapAddressesToNameServerList(List<String> addresses) {
        DnsResolverConfiguration.NameServersList result = new DnsResolverConfiguration.NameServersList();
        result.getNameServers().addAll(addresses);
        return result;
    }
}
