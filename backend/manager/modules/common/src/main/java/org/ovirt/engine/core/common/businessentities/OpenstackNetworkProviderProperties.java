package org.ovirt.engine.core.common.businessentities;

public class OpenstackNetworkProviderProperties extends TenantProviderProperties {

    private static final long serialVersionUID = -7470940167999871534L;

    private OpenstackNetworkPluginType pluginType;

    public OpenstackNetworkPluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(OpenstackNetworkPluginType pluginType) {
        this.pluginType = pluginType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getPluginType() == null) ? 0 : getPluginType().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OpenstackNetworkProviderProperties)) {
            return false;
        }
        OpenstackNetworkProviderProperties other = (OpenstackNetworkProviderProperties) obj;
        if (getPluginType() != other.getPluginType()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OpenstackNetworkProviderProperties [pluginType=")
                .append(getPluginType())
                .append(", tenantName=")
                .append(getTenantName())
                .append("]");
        return builder.toString();
    }

}
