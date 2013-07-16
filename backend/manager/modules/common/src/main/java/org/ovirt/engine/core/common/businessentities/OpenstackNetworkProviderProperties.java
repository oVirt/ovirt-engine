package org.ovirt.engine.core.common.businessentities;

public class OpenstackNetworkProviderProperties extends TenantProviderProperties {

    private static final long serialVersionUID = -7470940167999871534L;

    private String pluginType;

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    /**
     * Convenience method to know if the plugin represented is Linux Bridge.
     *
     * @return <code>true</code> iff the plugin type represents a Linux Bridge plugin.
     */
    public final boolean isLinuxBridge() {
        return OpenstackNetworkPluginType.LINUX_BRIDGE.name().equals(getPluginType());
    }

    /**
     * Convenience method to know if the plugin represented is Open vSwitch.
     *
     * @return <code>true</code> iff the plugin type represents a Open vSwitch plugin.
     */
    public final boolean isOpenVSwitch() {
        return OpenstackNetworkPluginType.OPEN_VSWITCH.name().equals(getPluginType());
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
        if (!getPluginType().equals(other.getPluginType())) {
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
