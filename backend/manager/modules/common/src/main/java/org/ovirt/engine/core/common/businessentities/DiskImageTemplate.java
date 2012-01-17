package org.ovirt.engine.core.common.businessentities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringFormat;

@Entity
@Table(name = "image_templates")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class DiskImageTemplate implements IImage,BusinessEntity<Guid> {
    private static final long serialVersionUID = -4485009586150352291L;

    public DiskImageTemplate() {
    }

    public DiskImageTemplate(Guid vtim_it_guid, Guid vmt_guid, String internal_drive_mapping, Guid it_guid, String os,
            String os_version, java.util.Date creation_date, long size, String description, Boolean bootable) {
        this.vtim_it_guidField = vtim_it_guid;
        this.vmTemplateId = vmt_guid;
        this.internal_drive_mappingField = internal_drive_mapping;
        this.id = it_guid;
        this.os = os;
        this.osVersion = os_version;
        this.creationDate = creation_date;
        this.size = size;
        this.description = description;
        this.bootable = bootable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((bootable == null) ? 0 : bootable.hashCode());
        result = prime
                * result
                + ((creationDate == null) ? 0 : creationDate
                        .hashCode());
        result = prime
                * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime
                * result
                + ((internal_drive_mappingField == null) ? 0
                        : internal_drive_mappingField.hashCode());
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((os == null) ? 0 : os.hashCode());
        result = prime * result
                + ((osVersion == null) ? 0 : osVersion.hashCode());
        result = prime * result + (int) (size ^ (size >>> 32));
        result = prime * result
                + ((vmTemplateId == null) ? 0 : vmTemplateId.hashCode());
        result = prime
                * result
                + ((vtim_it_guidField == null) ? 0 : vtim_it_guidField
                        .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiskImageTemplate other = (DiskImageTemplate) obj;
        if (bootable == null) {
            if (other.bootable != null)
                return false;
        } else if (!bootable.equals(other.bootable))
            return false;
        if (creationDate == null) {
            if (other.creationDate != null)
                return false;
        } else if (!creationDate.equals(other.creationDate))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (internal_drive_mappingField == null) {
            if (other.internal_drive_mappingField != null)
                return false;
        } else if (!internal_drive_mappingField
                .equals(other.internal_drive_mappingField))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (os == null) {
            if (other.os != null)
                return false;
        } else if (!os.equals(other.os))
            return false;
        if (osVersion == null) {
            if (other.osVersion != null)
                return false;
        } else if (!osVersion.equals(other.osVersion))
            return false;
        if (size != other.size)
            return false;
        if (vmTemplateId == null) {
            if (other.vmTemplateId != null)
                return false;
        } else if (!vmTemplateId.equals(other.vmTemplateId))
            return false;
        if (vtim_it_guidField == null) {
            if (other.vtim_it_guidField != null)
                return false;
        } else if (!vtim_it_guidField.equals(other.vtim_it_guidField))
            return false;
        return true;
    }

    @Transient
    @Type(type = "guid")
    private Guid vtim_it_guidField = new Guid();

    public Guid getvtim_it_guid() {
        return this.vtim_it_guidField;
    }

    public void setvtim_it_guid(Guid value) {
        this.vtim_it_guidField = value;
    }

    @Transient
    @Type(type = "guid")
    private Guid vmTemplateId = new Guid();

    public Guid getvmt_guid() {
        return this.vmTemplateId;
    }

    public void setvmt_guid(Guid value) {
        this.vmTemplateId = value;
    }

    @Transient
    private String internal_drive_mappingField;

    @Override
    public String getinternal_drive_mapping() {
        return this.internal_drive_mappingField;
    }

    @Override
    public void setinternal_drive_mapping(String value) {
        this.internal_drive_mappingField = value;
    }

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "it_guid")
    @Type(type = "guid")
    private Guid id = new Guid();

    @Override
    public Guid getit_guid() {
        return getId();
    }

    @Override
    public void setit_guid(Guid value) {
        setId(value);
    }

    @Column(name = "os", length = 40)
    private String os;

    public String getos() {
        return this.os;
    }

    public void setos(String value) {
        this.os = value;
    }

    @Column(name = "os_version", length = 40)
    private String osVersion;

    public String getos_version() {
        return this.osVersion;
    }

    public void setos_version(String value) {
        this.osVersion = value;
    }

    @Column(name = "creation_date", nullable = false)
    private java.util.Date creationDate = new java.util.Date(0);

    @Override
    public java.util.Date getcreation_date() {
        return this.creationDate;
    }

    @Override
    public void setcreation_date(java.util.Date value) {
        this.creationDate = value;
    }

    @Column(name = "size", nullable = false)
    private long size;

    @Override
    public long getsize() {
        return this.size;
    }

    @Override
    public void setsize(long value) {
        this.size = value;
    }

    @Column(name = "description", length = 4000)
    private String description;

    @Override
    public String getdescription() {
        return this.description;
    }

    @Override
    public void setdescription(String value) {
        this.description = value;
    }

    @Column(name = "bootable")
    private Boolean bootable;

    public Boolean getbootable() {
        return this.bootable;
    }

    public void setbootable(Boolean value) {
        this.bootable = value;
    }

    @Override
    public String toString() {
        return StringFormat.format("'%1$s' - uid = '%2$s' , description = '%3$s'", this.getinternal_drive_mapping(),
                this.getit_guid(), this.getdescription());
    }

    @Override
    public Guid getcontainer_guid() {
        return getvmt_guid();
    }

    @Override
    public void setcontainer_guid(Guid value) {
        setvmt_guid(value);
    }

    @Override
    public int getread_rate_kb_per_sec() {
        return -1;
    }

    @Override
    public void setread_rate_kb_per_sec(int value) {
        throw new RuntimeException("The method or operation is not implemented.");
    }

    @Override
    public int getwrite_rate_kb_per_sec() {
        return -1;
    }

    @Override
    public void setwrite_rate_kb_per_sec(int value) {
        throw new RuntimeException("The method or operation is not implemented.");
    }

    @XmlElement
    public long getSizeInGigabyte() {
        return getsize() / (1024 * 1024 * 1024);
    }

    public void setSizeInGigabyte(long value) {
        setsize(value * (1024 * 1024 * 1024));
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

}
