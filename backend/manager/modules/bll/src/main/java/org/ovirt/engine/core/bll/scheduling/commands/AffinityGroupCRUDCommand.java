package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public abstract class AffinityGroupCRUDCommand extends CommandBase<AffinityGroupCRUDParameters> {

    AffinityGroup affinityGroup = null;

    public AffinityGroupCRUDCommand(AffinityGroupCRUDParameters parameters) {
        super(parameters);
        if (getAffinityGroup() != null) {
            setVdsGroupId(getAffinityGroup().getClusterId());
            addCustomValue("affinityGroupName", getAffinityGroup().getName());
        }
    }

    protected boolean validateParameters() {
        if (getVdsGroup() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_CLUSTER_FOR_AFFINITY_GROUP);
        }
        if (getParameters().getAffinityGroup().getEntityIds() != null) {
            VmStatic vmStatic = null;
            Set<Guid> vmSet = new HashSet<>();
            for (Guid vmId : getParameters().getAffinityGroup().getEntityIds()) {
                vmStatic = getVmStaticDAO().get(vmId);
                if (vmStatic == null) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_VM_FOR_AFFINITY_GROUP);
                }
                if (!Objects.equals(vmStatic.getVdsGroupId(), getVdsGroupId())) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_IN_AFFINITY_GROUP_CLUSTER);
                }
                if (vmSet.contains(vmStatic.getId())) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DUPLICTE_VM_IN_AFFINITY_GROUP);
                } else {
                    vmSet.add(vmStatic.getId());
                }
            }
        }
        return true;
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
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
    }

    protected Guid getClusterId() {
        return getAffinityGroup().getClusterId();
    }

    protected AffinityGroupDao getAffinityGroupDao() {
        return DbFacade.getInstance().getAffinityGroupDao();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__AFFINITY_GROUP);
    }
}
