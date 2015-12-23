package org.ovirt.engine.core.common.scheduling;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

/**
 * Bussiness entity for cluster policy, holds logic for host selections and load balancing logic.
 */
public class ClusterPolicy implements BusinessEntity<Guid>, IVdcQueryable, Nameable {
    private static final long serialVersionUID = 7739745365583134911L;
    public static final Guid UPGRADE_POLICY_GUID =
            Guid.createGuidFromString("8d5d7bec-68de-4a67-b53e-0ac54686d586");

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
     * Map of filters positions: [uuid (policy unit id), int (position)] Acceptable position values: first (-1), last
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

    public boolean isClusterUpgradePolicy() {
        return UPGRADE_POLICY_GUID.equals(id);
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
        return Objects.hash(
                balance,
                defaultPolicy,
                description,
                filterPositionMap,
                filters,
                functions,
                id,
                locked,
                name,
                parameterMap
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClusterPolicy)) {
            return false;
        }
        ClusterPolicy other = (ClusterPolicy) obj;
        return Objects.equals(balance, other.balance)
                && defaultPolicy == other.defaultPolicy
                && Objects.equals(description, other.description)
                && Objects.equals(filterPositionMap, other.filterPositionMap)
                && Objects.equals(filters, other.filters)
                && Objects.equals(functions, other.functions)
                && Objects.equals(id, other.id)
                && locked == other.locked
                && Objects.equals(name, other.name)
                && Objects.equals(parameterMap, other.parameterMap);
    }

}
