package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Provider.AdditionalProperties;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class OpenStackProviderProperties implements AdditionalProperties {

    private static final long serialVersionUID = 573702404083234015L;

    private String tenantName;
    private String userDomainName;
    private String projectName;
    private String projectDomainName;

    public OpenStackProviderProperties() {
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getUserDomainName() {
        return userDomainName;
    }

    public void setUserDomainName(String userDomainName) {
        this.userDomainName = userDomainName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDomainName() {
        return projectDomainName;
    }

    public void setProjectDomainName(String projectDomainName) {
        this.projectDomainName = projectDomainName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantName, userDomainName, projectName, projectDomainName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpenStackProviderProperties)) {
            return false;
        }
        OpenStackProviderProperties other = (OpenStackProviderProperties) obj;
        return Objects.equals(tenantName, other.tenantName)
                && Objects.equals(userDomainName, other.userDomainName)
                && Objects.equals(projectName, other.projectName)
                && Objects.equals(projectDomainName, other.projectDomainName);
    }

    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return tsb
                .append("tenantName", getTenantName())
                .append("userDomainName", getUserDomainName())
                .append("projectName", getProjectName())
                .append("projectDomainName", getProjectDomainName());
    }

    @Override
    public String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }
}
