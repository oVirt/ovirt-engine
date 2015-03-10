package org.ovirt.engine.core.common.scheduling;

import java.io.Serializable;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.compat.Guid;

/**
 * Policy unit BE represents scheduling unit with the following methods: filter, score & balance.
 */
public class PolicyUnit extends IVdcQueryable implements BusinessEntity<Guid>, Serializable, Nameable {
    private static final long serialVersionUID = 7739555364433134921L;

    /**
     * entity unique identifier
     */
    private Guid id;
    /**
     * policy unit name.
     */
    private String name;
    /**
     * policy unit description.
     */
    private String description;
    /**
     * policy unit that is implemented in the system, or loaded externally.
     */
    private boolean internal;
    /**
     * specifies policy unit type (filter, weight or load balance)
     */
    private PolicyUnitType policyUnitType;
    /**
     * policy unit acceptable custom parameters; format <parameterName, regex>
     */
    private Map<String, String> parameterRegExMap;
    /**
     * only for external units, marks if it exists on disk
     */
    private boolean enabled;

    public PolicyUnit() {
        internal = true;
        enabled = true;
    }

    @Override
    public Object getQueryableId() {
        return getId();
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

    public boolean isInternal() {
        return internal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public PolicyUnitType getPolicyUnitType() {
        return policyUnitType;
    }

    public void setPolicyUnitType(PolicyUnitType policyUnitType) {
        this.policyUnitType = policyUnitType;
    }

    public Map<String, String> getParameterRegExMap() {
        return parameterRegExMap;
    }

    public void setParameterRegExMap(Map<String, String> parameterRegExMap) {
        this.parameterRegExMap = parameterRegExMap;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((policyUnitType == null) ? 0 : policyUnitType.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (internal ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameterRegExMap == null) ? 0 : parameterRegExMap.hashCode());
        result = prime * result + (enabled ? 1231 : 1237);
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
        PolicyUnit other = (PolicyUnit) obj;
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
        if (internal != other.internal)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parameterRegExMap == null) {
            if (other.parameterRegExMap != null)
                return false;
        } else if (!parameterRegExMap.equals(other.parameterRegExMap))
            return false;
        if (policyUnitType != other.policyUnitType)
            return false;
        if (enabled != other.enabled) {
            return false;
        }
        return true;
    }

}
