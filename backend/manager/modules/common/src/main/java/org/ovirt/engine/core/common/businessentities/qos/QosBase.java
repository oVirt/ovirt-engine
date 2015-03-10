package org.ovirt.engine.core.common.businessentities.qos;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.compat.Guid;

/**
 * Base class for QoS objects derived class will hold qos limit according to type.
 */
public class QosBase extends IVdcQueryable implements BusinessEntity<Guid>, Serializable, Nameable {

    private static final String UNLIMITED = "Unlimited";
    private static final long serialVersionUID = 1122772549710787678L;
    private Guid id = Guid.Empty;
    private QosType qosType;

    @NotNull(message = "ACTION_TYPE_FAILED_QOS_STORAGE_POOL_NOT_EXIST")
    private Guid storagePoolId;

    @NotNull(message = "QOS_NAME_NOT_NULL")
    @Size(min = 1, max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE, message = "QOS_NAME_TOO_LONG")
    @ValidI18NName(message = "QOS_NAME_INVALID")
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;

    @SuppressWarnings("unused")
    private QosBase() {
    }

    public QosBase(QosType qosType) {
        if (qosType == null) {
            throw new IllegalArgumentException("Quality of Service element type cannot be null");
        }
        this.qosType = qosType;
    }
    /**
     * @return object's type
     */
    public QosType getQosType() {
        return qosType;
    }

    /**
     * Extended of toString(), should include more inputs, and be called explicitly.
     *
     * @return object summary
     */
    public String getString() {
        return toString();
    }

    protected String renderQosParameter(Object qosParameter) {
        return (qosParameter == null) ? UNLIMITED : String.valueOf(qosParameter);
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((qosType == null) ? 0 : qosType.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
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
        QosBase other = (QosBase) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (qosType != other.qosType)
            return false;
        if (storagePoolId == null) {
            if (other.storagePoolId != null)
                return false;
        } else if (!storagePoolId.equals(other.storagePoolId))
            return false;
        return true;
    }

}
