package org.ovirt.engine.core.common.scheduling;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

/**
 * Bussiness entity for cluster policy, holds logic for host selections and load balancing logic.
 */
public class ClusterPolicy extends IVdcQueryable implements BusinessEntity<Guid>, Serializable, Nameable {
    private static final long serialVersionUID = 7739745365583134911L;

    /**
     * entity unique identifier
     */
    private Guid id;
    /**
     * policy name
     */
    private String name;
    /**
     * policy description
     */
    private String description;
    /**
     * indicates whether its an internal cluster policy, that provided with the system, and can't be removed
     */
    private boolean locked;
    /**
     * this policy will be attached to cluster in case no policy is specified
     */
    private boolean defaultPolicy;

    /**
     * set of policy units ids, specifies which filters need to be executed in filtering
     */
    private ArrayList<Guid> filters;
    /**
     * Map of filters positions: <uuid (policy unit id), int (position)> Acceptable position values: first (-1), last
     * (1), no position (0)
     */
    private Map<Guid, Integer> filterPositionMap;
    /**
     * set of policy units, specifies which weight functions will be executed upon selection each function has a factor
     * attached.
     */
    private ArrayList<Pair<Guid, Integer>> functions;
    /**
     * policy unit id, that indicates which balance logic to execute
     */
    private Guid balance;
    /**
     * Map of custom properties for policy
     */
    private Map<String, String> parameterMap;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isDefaultPolicy() {
        return defaultPolicy;
    }

    public void setDefaultPolicy(boolean defaultPolicy) {
        this.defaultPolicy = defaultPolicy;
    }

    public ArrayList<Guid> getFilters() {
        return filters;
    }

    public void setFilters(ArrayList<Guid> filters) {
        this.filters = filters;
    }

    public Map<Guid, Integer> getFilterPositionMap() {
        return filterPositionMap;
    }

    public void setFilterPositionMap(Map<Guid, Integer> filterPositionMap) {
        this.filterPositionMap = filterPositionMap;
    }

    public ArrayList<Pair<Guid, Integer>> getFunctions() {
        return functions;
    }

    public void setFunctions(ArrayList<Pair<Guid, Integer>> functions) {
        this.functions = functions;
    }

    public Guid getBalance() {
        return balance;
    }

    public void setBalance(Guid balance) {
        this.balance = balance;
    }

    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, String> parameterMap) {
        this.parameterMap = parameterMap;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((balance == null) ? 0 : balance.hashCode());
        result = prime * result + (defaultPolicy ? 1231 : 1237);
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((filterPositionMap == null) ? 0 : filterPositionMap.hashCode());
        result = prime * result + ((filters == null) ? 0 : filters.hashCode());
        result = prime * result + ((functions == null) ? 0 : functions.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (locked ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameterMap == null) ? 0 : parameterMap.hashCode());
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
        ClusterPolicy other = (ClusterPolicy) obj;
        if (balance == null) {
            if (other.balance != null)
                return false;
        } else if (!balance.equals(other.balance))
            return false;
        if (defaultPolicy != other.defaultPolicy)
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (filterPositionMap == null) {
            if (other.filterPositionMap != null)
                return false;
        } else if (!filterPositionMap.equals(other.filterPositionMap))
            return false;
        if (filters == null) {
            if (other.filters != null)
                return false;
        } else if (!filters.equals(other.filters))
            return false;
        if (functions == null) {
            if (other.functions != null)
                return false;
        } else if (!functions.equals(other.functions))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (locked != other.locked)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parameterMap == null) {
            if (other.parameterMap != null)
                return false;
        } else if (!parameterMap.equals(other.parameterMap))
            return false;
        return true;
    }

}
