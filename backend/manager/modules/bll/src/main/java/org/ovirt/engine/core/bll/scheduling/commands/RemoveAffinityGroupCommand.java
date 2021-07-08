package org.ovirt.engine.core.bll.scheduling.commands;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class RemoveAffinityGroupCommand extends AffinityGroupCRUDCommand<AffinityGroupCRUDParameters> {

    @Inject
    private AffinityGroupDao affinityGroupDao;

    @Inject
    private VmStaticDao vmStaticDao;

    public RemoveAffinityGroupCommand(AffinityGroupCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getAffinityGroup() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        vmStaticDao.incrementDbGenerationForVms(getAffinityGroup().getVmIds());
        affinityGroupDao.remove(getParameters().getAffinityGroupId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVED_AFFINITY_GROUP
                : AuditLogType.USER_FAILED_TO_REMOVE_AFFINITY_GROUP;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }
}
