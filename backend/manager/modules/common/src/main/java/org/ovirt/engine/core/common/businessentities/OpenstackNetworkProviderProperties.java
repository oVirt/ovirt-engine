package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class OpenstackNetworkProviderProperties extends OpenStackProviderProperties {

    private static final long serialVersionUID = -7470940167999871534L;

    private String pluginType;

    private boolean readOnly = true;

    public boolean getReadOnly(){
        return readOnly;
    }

    public void setReadOnly(boolean entity) {
        this.readOnly = entity;
    }

    private boolean autoSync;

    public boolean getAutoSync() {
        return autoSync;
    }

    public void setAutoSync(boolean autoSync) {
        this.autoSync = autoSync;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                pluginType
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpenstackNetworkProviderProperties)) {
            return false;
        }
        OpenstackNetworkProviderProperties other = (OpenstackNetworkProviderProperties) obj;
        return super.equals(obj)
                && Objects.equals(pluginType, other.pluginType);
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("pluginType", getPluginType());
    }
}
