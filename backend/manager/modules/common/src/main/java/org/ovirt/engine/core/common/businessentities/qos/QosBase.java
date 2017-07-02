package org.ovirt.engine.core.common.businessentities.qos;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.compat.Guid;

/**
 * Base class for QoS objects derived class will hold qos limit according to type.
 */
public class QosBase implements Queryable, BusinessEntity<Guid>, Nameable {

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
        return Objects.hash(
                description,
                id,
                name,
                qosType,
                storagePoolId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QosBase)) {
            return false;
        }
        QosBase other = (QosBase) obj;
        return Objects.equals(description, other.description)
                && Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && qosType == other.qosType
                && Objects.equals(storagePoolId, other.storagePoolId);
    }

}
