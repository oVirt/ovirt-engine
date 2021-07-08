package org.ovirt.engine.core.bll.scheduling.commands;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupMemberChangeParameters;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class AddHostToAffinityGroupCommand <T extends AffinityGroupMemberChangeParameters>
        extends EditAffinityGroupCommand<T> {
    @Inject
    private AffinityGroupDao affinityGroupDao;

    @Inject
    private VdsStaticDao vdsStaticDao;

    public AddHostToAffinityGroupCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getAffinityGroup() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_AFFINITY_GROUP_ID);
        }
        if (vdsStaticDao.get(getParameters().getEntityId()) == null) {
            return failValidation(EngineMessage.VDS_DOES_NOT_EXIST);
        }
        if (getAffinityGroup().getVdsIds().contains(getParameters().getEntityId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_ENTITY_IN_AFFINITY_GROUP, String
                    .format("$entity %s", Entity_VDS));
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        affinityGroupDao.insertAffinityHost(getParameters().getAffinityGroupId(), getParameters().getEntityId());
        setSucceeded(true);
    }
}
