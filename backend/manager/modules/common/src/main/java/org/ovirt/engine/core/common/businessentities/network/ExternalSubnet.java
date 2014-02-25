package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;

public class ExternalSubnet extends IVdcQueryable implements Serializable, Nameable {

    private static final long serialVersionUID = 7357288865938773402L;

    @NotNull(groups = { RemoveEntity.class })
    private String id;

    private String name;

    private String cidr;

    private IpVersion ipVersion;

    @NotNull(groups = { RemoveEntity.class })
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
        StringBuilder builder = new StringBuilder();
        builder.append("Subnet [id=")
                .append(getId())
                .append(", name=")
                .append(getName())
                .append(", cidr=")
                .append(getCidr())
                .append(", ipVersion=")
                .append(getIpVersion())
                .append(", externalNetwork=")
                .append(getExternalNetwork())
                .append(", gateway=")
                .append(getGateway())
                .append(", dnsServers=")
                .append(getDnsServers())
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCidr() == null) ? 0 : getCidr().hashCode());
        result = prime * result + ((getExternalNetwork() == null) ? 0 : getExternalNetwork().hashCode());
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getIpVersion() == null) ? 0 : getIpVersion().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getGateway() == null) ? 0 : getGateway().hashCode());
        result = prime * result + ((getDnsServers() == null) ? 0 : getDnsServers().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExternalSubnet)) {
            return false;
        }
        ExternalSubnet other = (ExternalSubnet) obj;
        if (getCidr() == null) {
            if (other.getCidr() != null) {
                return false;
            }
        } else if (!getCidr().equals(other.getCidr())) {
            return false;
        }
        if (getExternalNetwork() == null) {
            if (other.getExternalNetwork() != null) {
                return false;
            }
        } else if (!getExternalNetwork().equals(other.getExternalNetwork())) {
            return false;
        }
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        if (getIpVersion() != other.getIpVersion()) {
            return false;
        }
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (getGateway() == null) {
            if (other.getGateway() != null) {
                return false;
            }
        } else if (!getGateway().equals(other.getGateway())) {
            return false;
        }
        if (getDnsServers() == null) {
            if (other.getDnsServers() != null) {
                return false;
            }
        } else if (!getDnsServers().equals(other.getDnsServers())) {
            return false;
        }
        return true;
    }

    public enum IpVersion {
        IPV4,
        IPV6
    }
}
