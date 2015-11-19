package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class TenantProviderProperties implements AdditionalProperties {

    private static final long serialVersionUID = 573702404083234015L;

    private String tenantName;

    public TenantProviderProperties() {
    }

    public TenantProviderProperties(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tenantName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TenantProviderProperties)) {
            return false;
        }
        TenantProviderProperties other = (TenantProviderProperties) obj;
        return Objects.equals(tenantName, other.tenantName);
    }

    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return tsb.append("tenantName", getTenantName());
    }

    @Override
    public String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }
}
