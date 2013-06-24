package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.utils.customprop.ValidationError;

public abstract class ClusterPolicyCRUDCommand extends CommandBase<ClusterPolicyCRUDParameters> {
    private ClusterPolicy clusterPolicy;

    public ClusterPolicyCRUDCommand(ClusterPolicyCRUDParameters parameters) {
        super(parameters);
        setClusterPolicy(getParameters().getClusterPolicy());
        addCustomValue("ClusterPolicy", getClusterPolicy().getName());
        getParameters().setShouldBeLogged(true);
    }

    protected boolean checkAddEditValidations() {
        List<ClusterPolicy> clusterPolicies = SchedulingManager.getInstance().getClusterPolicies();
        if (getClusterPolicy() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID);
        }
        for (ClusterPolicy clusterPolicy : clusterPolicies) {
            if (!clusterPolicy.getId().equals(getClusterPolicy().getId()) &&
                    clusterPolicy.getName().equals(getClusterPolicy().getName())) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_NAME_INUSE);
            }
        }
        Map<Guid, PolicyUnitImpl> map = SchedulingManager.getInstance().getPolicyUnitsMap();
        // check filter policy units
        if (getClusterPolicy().getFilters() != null) {
            for (Guid filterId : getClusterPolicy().getFilters()) {
                PolicyUnitImpl policyUnitImpl = map.get(filterId);
                if (policyUnitImpl == null) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_UNKNOWN_POLICY_UNIT);
                }
                if (!policyUnitImpl.isFilterImplemeted()) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_FILTER_NOT_IMPLEMENTED);
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
                        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_ONLY_ONE_FILTER_CAN_BE_FIRST);
                    }
                } else if (position == 1) {
                    if (!hasLast) {
                        hasLast = true;
                    } else {
                        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_ONLY_ONE_FILTER_CAN_BE_LAST);
                    }
                }
            }
        }
        // check function policy units
        if (getClusterPolicy().getFunctions() != null) {
            for (Pair<Guid, Integer> functionPair : getClusterPolicy().getFunctions()) {
                PolicyUnitImpl policyUnitImpl = map.get(functionPair.getFirst());
                if (policyUnitImpl == null) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_UNKNOWN_POLICY_UNIT);
                }
                if (!policyUnitImpl.isFunctionImplemeted()) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_FUNCTION_NOT_IMPLEMENTED);
                }
                if (functionPair.getSecond() < 0) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_FUNCTION_FACTOR_NEGATIVE);
                }
            }
        }
        // check balance policy unit
        if (getClusterPolicy().getBalance() != null) {
            PolicyUnitImpl policyUnitImpl = map.get(getClusterPolicy().getBalance());
            if (policyUnitImpl == null) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_UNKNOWN_POLICY_UNIT);
            }
            if (!policyUnitImpl.isBalanceImplemeted()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_BALANCE_NOT_IMPLEMENTED);
            }
        }

        List<ValidationError> validationErrors =
                SimpleCustomPropertiesUtil.getInstance().validateProperties(SchedulingManager.getInstance()
                .getCustomPropertiesRegexMap(getClusterPolicy()),
                getClusterPolicy().getParameterMap());
        if (!validationErrors.isEmpty()) {
            SimpleCustomPropertiesUtil.getInstance().handleCustomPropertiesError(validationErrors,
                    getReturnValue().getCanDoActionMessages());
            return false;
        }
        return true;
    }

    protected boolean checkRemoveEditValidations() {
        Guid clusterPolicyId = getParameters().getClusterPolicyId();
        if (clusterPolicyId == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID);
        }
        ClusterPolicy clusterPolicy = SchedulingManager.getInstance().getClusterPolicy(clusterPolicyId);
        if (clusterPolicy == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID);
        }
        if (clusterPolicy.isLocked()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_POLICY_PARAMETERS_INVALID);
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
