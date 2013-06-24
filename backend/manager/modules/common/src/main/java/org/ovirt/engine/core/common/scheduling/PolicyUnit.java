package org.ovirt.engine.core.common.scheduling;

import java.io.Serializable;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Guid;

/**
 * Policy unit BE represents scheduling unit with the following methods: filter, score & balance.
 */
public class PolicyUnit extends IVdcQueryable implements BusinessEntity<Guid>, Serializable {
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
     * policy unit that is implemented in the system, or loaded externally.
     */
    private boolean internal = true;
    /**
     * specifies whether filter method implemented in loaded class
     */
    private boolean filterImplemeted;
    /**
     * specifies whether weight function method implemented in loaded class
     */
    private boolean functionImplemeted;
    /**
     * specifies whether load balancing method implemented in loaded class
     */
    private boolean balanceImplemeted;
    /**
     * policy unit acceptable custom parameters; format <parameterName, regex>
     */
    private Map<String, String> parameterRegExMap;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public boolean isFilterImplemeted() {
        return filterImplemeted;
    }

    public void setFilterImplemeted(boolean filterImplemeted) {
        this.filterImplemeted = filterImplemeted;
    }

    public boolean isFunctionImplemeted() {
        return functionImplemeted;
    }

    public void setFunctionImplemeted(boolean functionImplemeted) {
        this.functionImplemeted = functionImplemeted;
    }

    public boolean isBalanceImplemeted() {
        return balanceImplemeted;
    }

    public void setBalanceImplemeted(boolean balanceImplemeted) {
        this.balanceImplemeted = balanceImplemeted;
    }

    public Map<String, String> getParameterRegExMap() {
        return parameterRegExMap;
    }

    public void setParameterRegExMap(Map<String, String> parameterRegExMap) {
        this.parameterRegExMap = parameterRegExMap;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (balanceImplemeted ? 1231 : 1237);
        result = prime * result + (filterImplemeted ? 1231 : 1237);
        result = prime * result + (functionImplemeted ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (internal ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameterRegExMap == null) ? 0 : parameterRegExMap.hashCode());
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
        if (balanceImplemeted != other.balanceImplemeted)
            return false;
        if (filterImplemeted != other.filterImplemeted)
            return false;
        if (functionImplemeted != other.functionImplemeted)
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
        return true;
    }

}
