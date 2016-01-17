package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public abstract class AffinityGroupCRUDCommand extends CommandBase<AffinityGroupCRUDParameters> {

    AffinityGroup affinityGroup = null;

    public AffinityGroupCRUDCommand(AffinityGroupCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        if (getAffinityGroup() != null) {
            setClusterId(getAffinityGroup().getClusterId());
            addCustomValue("affinityGroupName", getAffinityGroup().getName());
        }
    }

    protected boolean validateParameters() {
        if (getCluster() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_CLUSTER_FOR_AFFINITY_GROUP);
        }
        if (getParameters().getAffinityGroup().getEntityIds() != null) {
            VmStatic vmStatic = null;
            Set<Guid> vmSet = new HashSet<>();
            for (Guid vmId : getParameters().getAffinityGroup().getEntityIds()) {
                vmStatic = getVmStaticDao().get(vmId);
                if (vmStatic == null) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_VM_FOR_AFFINITY_GROUP);
                }
                if (!Objects.equals(vmStatic.getClusterId(), getClusterId())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_IN_AFFINITY_GROUP_CLUSTER);
                }
                if (vmSet.contains(vmStatic.getId())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_DUPLICTE_VM_IN_AFFINITY_GROUP);
                } else {
                    vmSet.add(vmStatic.getId());
                }
            }
        }

        return affinityGroupsWithoutConflict(getParameters().getAffinityGroup());
    }

    private boolean affinityGroupsWithoutConflict(AffinityGroup affinityGroup) {
        List<AffinityGroup> affinityGroups = getAffinityGroupDao().getAllAffinityGroupsByClusterId(affinityGroup.getClusterId());

        // Replace the existing affinity group by the updated copy
        for(Iterator<AffinityGroup> it = affinityGroups.iterator(); it.hasNext(); ) {
            AffinityGroup g = it.next();
            if (g.getId().equals(affinityGroup.getId())) {
                it.remove();
            }
        }
        affinityGroups.add(affinityGroup);

        Set<Set<Guid>> unifiedPositive = AffinityRulesUtils.getUnifiedPositiveAffinityGroups(affinityGroups);
        AffinityRulesUtils.AffinityGroupConflict result =
                AffinityRulesUtils.checkForAffinityGroupConflict(
                        affinityGroups,
                        unifiedPositive);

        if (result == null) {
            return true;
        } else {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_RULES_COLLISION,
                    String.format("$UnifiedAffinityGroups %s", result.getPositiveVms().toString()),
                    String.format("$negativeAR %s", result.getNegativeVms().toString()));
        }
    }

    protected AffinityGroup getAffinityGroup() {
        if (affinityGroup == null) {
            affinityGroup = getAffinityGroupDao().get(getParameters().getAffinityGroupId());
        }
        return affinityGroup;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getClusterId(),
                VdcObjectType.Cluster,
                getActionType().getActionGroup()));
    }

    @Override
    public Guid getClusterId() {
        return getAffinityGroup().getClusterId();
    }

    protected AffinityGroupDao getAffinityGroupDao() {
        return DbFacade.getInstance().getAffinityGroupDao();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__AFFINITY_GROUP);
    }
}
