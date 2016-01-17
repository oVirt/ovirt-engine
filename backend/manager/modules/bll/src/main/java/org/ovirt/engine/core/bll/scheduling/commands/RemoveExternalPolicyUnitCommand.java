package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.RemoveExternalPolicyUnitParameters;
import org.ovirt.engine.core.compat.Guid;

public class RemoveExternalPolicyUnitCommand extends CommandBase<RemoveExternalPolicyUnitParameters> {

    public RemoveExternalPolicyUnitCommand(RemoveExternalPolicyUnitParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Inject
    private SchedulingManager schedulingManager;

    @Override
    protected boolean validate() {
        if(!schedulingManager.getPolicyUnitsMap().containsKey(getPolicyUnitId())){
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_POLICY_UNKNOWN_POLICY_UNIT);
        }
        List<String> clusterPoliciesNames =
                schedulingManager.getClusterPoliciesNamesByPolicyUnitId(getPolicyUnitId());
        if (clusterPoliciesNames != null && clusterPoliciesNames.size() > 0) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_POLICY_UNIT_ATTACHED_TO_CLUSTER_POLICY,
                    String.format("$cpNames %1$s", StringUtils.join(clusterPoliciesNames, ',')));
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        schedulingManager.removeExternalPolicyUnit(getPolicyUnitId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__POLICY_UNIT);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }

    private Guid getPolicyUnitId() {
        return getParameters().getPolicyUnitId();
    }
}
