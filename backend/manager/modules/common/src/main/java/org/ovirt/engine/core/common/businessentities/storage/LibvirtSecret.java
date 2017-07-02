package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Date;
import java.util.Objects;

import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;

public class LibvirtSecret implements Queryable, BusinessEntity<Guid> {

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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LibvirtSecret)) {
            return false;
        }
        LibvirtSecret other = (LibvirtSecret) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(value, other.value)
                && usageType == other.usageType
                && Objects.equals(description, other.description)
                && Objects.equals(providerId, other.providerId)
                && Objects.equals(creationDate, other.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                value,
                usageType,
                description,
                providerId,
                creationDate
        );
    }
}
