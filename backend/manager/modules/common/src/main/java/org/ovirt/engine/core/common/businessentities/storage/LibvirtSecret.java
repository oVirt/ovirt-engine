package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Date;

import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;

public class LibvirtSecret implements IVdcQueryable, BusinessEntity<Guid> {

    private Guid id;
    @Pattern(regexp = ValidationUtils.BASE_64_PATTERN, message = "LIBVIRT_SECRET_VALUE_ILLEGAL_FORMAT")
    private String value;
    private LibvirtSecretUsageType usageType;
    private String description;
    private Guid providerId;
    private Date creationDate;

    public LibvirtSecret() {
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LibvirtSecretUsageType getUsageType() {
        return usageType;
    }

    public void setUsageType(LibvirtSecretUsageType usageType) {
        this.usageType = usageType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Guid getProviderId() {
        return providerId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setProviderId(Guid providerId) {
        this.providerId = providerId;
    }

    public Object getQueryableId() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LibvirtSecret that = (LibvirtSecret) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (value != null ? !value.equals(that.value) : that.value != null)
            return false;
        if (usageType != that.usageType)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (providerId != null ? !providerId.equals(that.providerId) : that.providerId != null)
            return false;
        return !(creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (usageType != null ? usageType.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (providerId != null ? providerId.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        return result;
    }
}
