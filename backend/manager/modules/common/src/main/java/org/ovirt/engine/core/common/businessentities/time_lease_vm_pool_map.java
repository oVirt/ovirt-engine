package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "time_lease_vm_pool_map")
@Entity
@Table(name = "time_lease_vm_pool_map")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class time_lease_vm_pool_map implements Serializable {
    private static final long serialVersionUID = 6618665699657228296L;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "id")),
            @AttributeOverride(name = "vmPoolId", column = @Column(name = "vm_pool_id")) })
    private time_lease_vm_pool_map_id id = new time_lease_vm_pool_map_id();

    public time_lease_vm_pool_map() {
    }

    public time_lease_vm_pool_map(Date end_time, Guid id, Date start_time, int type, Guid vm_pool_id) {
        this.endTime = end_time;
        this.id.id = id;
        this.startTime = start_time;
        this.type = type;
        this.id.vmPoolId = vm_pool_id;
    }

    @Column(name = "end_time", nullable = false)
    private Date endTime = new Date();

    @XmlElement
    public Date getend_time() {
        return this.endTime;
    }

    public void setend_time(Date value) {
        this.endTime = value;
    }

    @XmlElement
    public Guid getid() {
        return this.id.id;
    }

    public void setid(Guid value) {
        this.id.id = value;
    }

    @Column(name = "start_time", nullable = false)
    private Date startTime = new Date();

    @XmlElement
    public Date getstart_time() {
        return this.startTime;
    }

    public void setstart_time(Date value) {
        this.startTime = value;
    }

    @Column(name = "type", nullable = false)
    private Integer type = 0;

    @XmlElement
    public int gettype() {
        return this.type;
    }

    public void settype(int value) {
        this.type = value;
    }

    @XmlElement
    public Guid getvm_pool_id() {
        return this.id.vmPoolId;
    }

    public void setvm_pool_id(Guid value) {
        this.id.vmPoolId = value;
    }

    @Transient
    public time_lease_vm_pool_map oldMap;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((oldMap == null) ? 0 : oldMap.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        result = prime * result + type;
        result = prime * result + ((id == null || id.vmPoolId == null) ? 0 : id.vmPoolId.hashCode());
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
        time_lease_vm_pool_map other = (time_lease_vm_pool_map) obj;
        if (endTime == null) {
            if (other.endTime != null)
                return false;
        } else {
            // TODO remvoe this when moving to Hibernate
            long thisEndTimeNoMillis = endTime.getTime();
            thisEndTimeNoMillis -= thisEndTimeNoMillis % 1000l;

            long thatEndTimeNoMillis = other.endTime.getTime();
            thatEndTimeNoMillis -= thatEndTimeNoMillis % 1000l;

            if (thisEndTimeNoMillis != thatEndTimeNoMillis)
                return false;
        }
        // } else if (!endTime.equals(other.endTime))
        // return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (oldMap == null) {
            if (other.oldMap != null)
                return false;
        } else if (!oldMap.equals(other.oldMap))
            return false;
        if (startTime == null) {
            if (other.startTime != null)
                return false;
        } else {
            // TODO remove this when moving to Hibernate
            long thisStartTimeNoMillis = startTime.getTime();
            thisStartTimeNoMillis -= thisStartTimeNoMillis % 1000l;

            long thatStartTimeNoMillis = other.startTime.getTime();
            thatStartTimeNoMillis -= thatStartTimeNoMillis % 1000l;

            if (thisStartTimeNoMillis != thatStartTimeNoMillis)
                return false;

        }
        // } else if (!startTime.equals(other.startTime))
        // return false;
        if (!type.equals(other.type))
            return false;
        if (id.vmPoolId == null) {
            if (other.id.vmPoolId != null)
                return false;
        } else if (!id.vmPoolId.equals(other.id.vmPoolId))
            return false;
        return true;
    }
}
