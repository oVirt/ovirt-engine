package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class EditAffinityGroupCommand <T extends AffinityGroupCRUDParameters> extends AffinityGroupCRUDCommand<T> {

    @Inject
    private AffinityGroupDao affinityGroupDao;

    @Inject
    private VmStaticDao vmStaticDao;

    public EditAffinityGroupCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getAffinityGroup() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID);
        }
        if (!getParameters().getAffinityGroup().getClusterId().equals(getClusterId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_CHANGE_CLUSTER_ID);
        }
        if (!getAffinityGroup().getName().equals(getParameters().getAffinityGroup().getName()) &&
                affinityGroupDao.getByName(getParameters().getAffinityGroup().getName()) != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_AFFINITY_GROUP_NAME_EXISTS);
        }
        return validateParameters();
    }

    @Override
    protected void executeCommand() {
        Collection changedVms =
                CollectionUtils.disjunction(getAffinityGroup().getVmIds(), getParameters().getAffinityGroup()
                        .getVmIds());
        vmStaticDao.incrementDbGenerationForVms(new ArrayList<>(changedVms));
        affinityGroupDao.update(getParameters().getAffinityGroup());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_AFFINITY_GROUP
                : AuditLogType.USER_FAILED_TO_UPDATE_AFFINITY_GROUP;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }
}
