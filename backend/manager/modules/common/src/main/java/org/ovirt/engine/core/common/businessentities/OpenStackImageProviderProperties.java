package org.ovirt.engine.core.common.businessentities;

public class OpenStackImageProviderProperties extends TenantProviderProperties {

    private static final long serialVersionUID = -3887979451360188295L;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OpenStackImageProviderProperties [pluginType=")
                .append(", tenantName=")
                .append(getTenantName())
                .append("]");
        return builder.toString();
    }

}
