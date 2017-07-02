package org.ovirt.engine.core.common.scheduling;

import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

/**
 * Policy unit BE represents scheduling unit with the following methods: filter, score & balance.
 */
public class PolicyUnit implements BusinessEntity<Guid>, Queryable, Nameable {
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
     * policy unit acceptable custom parameters; format [parameterName, regex]
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
        return Objects.hash(
                policyUnitType,
                description,
                id,
                internal,
                name,
                parameterRegExMap,
                enabled
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PolicyUnit)) {
            return false;
        }
        PolicyUnit other = (PolicyUnit) obj;
        return Objects.equals(description, other.description)
                && Objects.equals(id, other.id)
                && internal == other.internal
                && Objects.equals(name, other.name)
                && Objects.equals(parameterRegExMap, other.parameterRegExMap)
                && policyUnitType == other.policyUnitType
                && enabled == other.enabled;
    }

}
