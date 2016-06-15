package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;

public abstract class ClusterPolicyCRUDCommand extends CommandBase<ClusterPolicyCRUDParameters> {
    private ClusterPolicy clusterPolicy;

    @Inject
    protected SchedulingManager schedulingManager;

    public ClusterPolicyCRUDCommand(ClusterPolicyCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setClusterPolicy(getParameters().getClusterPolicy());
        addCustomValue("ClusterPolicy", getClusterPolicy().getName());
        getParameters().setShouldBeLogged(true);
    }

    protected boolean checkAddEditValidations() {
        List<ClusterPolicy> clusterPolicies = schedulingManager.getClusterPolicies();
        if (getClusterPolicy() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID);
        }
        for (ClusterPolicy clusterPolicy : clusterPolicies) {
            if (!clusterPolicy.getId().equals(getClusterPolicy().getId()) &&
                    clusterPolicy.getName().equals(getClusterPolicy().getName())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_NAME_INUSE);
            }
        }
        Map<Guid, PolicyUnitImpl> map = schedulingManager.getPolicyUnitsMap();
        Set<Guid> existingPolicyUnits = new HashSet<>();
        // check filter policy units
        if (getClusterPolicy().getFilters() != null) {
            for (Guid filterId : getClusterPolicy().getFilters()) {
                if(isPolicyUnitExists(filterId, existingPolicyUnits)) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_DUPLICATE_POLICY_UNIT);
                }
                PolicyUnitImpl policyUnitImpl = map.get(filterId);
                if (policyUnitImpl == null) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_UNKNOWN_POLICY_UNIT);
                }
                if (policyUnitImpl.getPolicyUnit().getPolicyUnitType() != PolicyUnitType.FILTER) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_FILTER_NOT_IMPLEMENTED);
                }
            }
        }
        // check filters positions (there could be only one filter attached to first (-1) and last (-1)
        if (getClusterPolicy().getFilterPositionMap() != null) {
            boolean hasFirst = false;
            boolean hasLast = false;
            for (Integer position : getClusterPolicy().getFilterPositionMap().values()) {
                if (position == -1) {
                    if (!hasFirst) {
                        hasFirst = true;
                    } else {
                        return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_ONLY_ONE_FILTER_CAN_BE_FIRST);
                    }
                } else if (position == 1) {
                    if (!hasLast) {
                        hasLast = true;
                    } else {
                        return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_ONLY_ONE_FILTER_CAN_BE_LAST);
                    }
                }
            }
        }
        // check function policy units
        if (getClusterPolicy().getFunctions() != null) {
            for (Pair<Guid, Integer> functionPair : getClusterPolicy().getFunctions()) {
                if (isPolicyUnitExists(functionPair.getFirst(), existingPolicyUnits)) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_DUPLICATE_POLICY_UNIT);
                }
                PolicyUnitImpl policyUnitImpl = map.get(functionPair.getFirst());
                if (policyUnitImpl == null) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_UNKNOWN_POLICY_UNIT);
                }
                if (policyUnitImpl.getPolicyUnit().getPolicyUnitType() != PolicyUnitType.WEIGHT) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_FUNCTION_NOT_IMPLEMENTED);
                }
                if (functionPair.getSecond() < 0) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_FUNCTION_FACTOR_NEGATIVE);
                }
            }
        }
        // check balance policy unit
        if (getClusterPolicy().getBalance() != null) {
            PolicyUnitImpl policyUnitImpl = map.get(getClusterPolicy().getBalance());
            if (policyUnitImpl == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_UNKNOWN_POLICY_UNIT);
            }
            if (policyUnitImpl.getPolicyUnit().getPolicyUnitType() != PolicyUnitType.LOAD_BALANCING) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_BALANCE_NOT_IMPLEMENTED);
            }
        }
        // check selector policy unit
        if (getClusterPolicy().getSelector() != null) {
            PolicyUnitImpl policyUnitImpl = map.get(getClusterPolicy().getSelector());
            if (policyUnitImpl == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_UNKNOWN_POLICY_UNIT);
            }
            if (policyUnitImpl.getPolicyUnit().getPolicyUnitType() != PolicyUnitType.SELECTOR) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_SELECTOR_NOT_IMPLEMENTED);
            }
        }

        List<ValidationError> validationErrors =
                SimpleCustomPropertiesUtil.getInstance().validateProperties(schedulingManager
                .getCustomPropertiesRegexMap(getClusterPolicy()),
                getClusterPolicy().getParameterMap());
        if (!validationErrors.isEmpty()) {
            SimpleCustomPropertiesUtil.getInstance().handleCustomPropertiesError(validationErrors,
                    getReturnValue().getValidationMessages());
            return false;
        }
        return true;
    }

    private boolean isPolicyUnitExists(Guid policyUnitId, Set<Guid> existingPolicyUnits) {
        return !existingPolicyUnits.add(policyUnitId);
    }

    protected boolean checkRemoveEditValidations() {
        Guid clusterPolicyId = getParameters().getClusterPolicyId();
        if (clusterPolicyId == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID);
        }
        ClusterPolicy clusterPolicy = schedulingManager.getClusterPolicy(clusterPolicyId);
        if (clusterPolicy == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID);
        }
        if (clusterPolicy.isLocked()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID);
        }

        return true;
    }

    public ClusterPolicy getClusterPolicy() {
        return clusterPolicy;
    }

    public void setClusterPolicy(ClusterPolicy clusterPolicy) {
        this.clusterPolicy = clusterPolicy;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }
}
