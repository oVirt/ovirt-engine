package org.ovirt.engine.core.bll.scheduling.commands;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupMemberChangeParameters;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class AddVmToAffinityGroupCommand <T extends AffinityGroupMemberChangeParameters>
        extends EditAffinityGroupCommand<T> {
    @Inject
    private AffinityGroupDao affinityGroupDao;

    @Inject
    private VmStaticDao vmStaticDao;

    public AddVmToAffinityGroupCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getAffinityGroup() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID);
        }
        if (vmStaticDao.get(getParameters().getEntityId()) == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_EXIST);
        }
        if (getAffinityGroup().getVmIds().contains(getParameters().getEntityId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_ENTITY_IN_AFFINITY_GROUP, String
                    .format("$entity %s", Entity_VM));
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        vmStaticDao.incrementDbGeneration(getParameters().getEntityId());
        affinityGroupDao.insertAffinityVm(getParameters().getAffinityGroupId(), getParameters().getEntityId());
        setSucceeded(true);
    }
}
