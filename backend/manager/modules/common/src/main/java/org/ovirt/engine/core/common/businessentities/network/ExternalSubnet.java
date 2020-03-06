package org.ovirt.engine.core.common.businessentities.network;

import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.Cidr;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;

@Cidr
public class ExternalSubnet implements Queryable, Nameable {

    private static final long serialVersionUID = 7357288865938773402L;

    @NotNull(groups = RemoveEntity.class)
    private String id;

    private String name;

    private String cidr;

    private IpVersion ipVersion;

    @NotNull(groups = RemoveEntity.class)
    private ProviderNetwork externalNetwork;

    private String gateway;

    private List<String> dnsServers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public IpVersion getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(IpVersion ipVersion) {
        this.ipVersion = ipVersion;
    }

    public ProviderNetwork getExternalNetwork() {
        return externalNetwork;
    }

    public void setExternalNetwork(ProviderNetwork externalNetwork) {
        this.externalNetwork = externalNetwork;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public List<String> getDnsServers() {
        return dnsServers;
    }

    public void setDnsServers(List<String> dnsServers) {
        this.dnsServers = dnsServers;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("name", getName())
                .append("cidr", getCidr())
                .append("ipVersion", getIpVersion())
                .append("externalNetwork", getExternalNetwork())
                .append("gateway", getGateway())
                .append("dnsServers", getDnsServers())
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                cidr,
                externalNetwork,
                id,
                ipVersion,
                name,
                gateway,
                dnsServers
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExternalSubnet)) {
            return false;
        }
        ExternalSubnet other = (ExternalSubnet) obj;
        return Objects.equals(cidr, other.cidr)
                && Objects.equals(externalNetwork, other.externalNetwork)
                && Objects.equals(id, other.id)
                && ipVersion == other.ipVersion
                && Objects.equals(name, other.name)
                && Objects.equals(gateway, other.gateway)
                && Objects.equals(dnsServers, other.dnsServers);
    }

    public enum IpVersion {
        IPV4,
        IPV6
    }
}
