package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class DetachVmFromTagCommand<T extends AttachEntityToTagParameters> extends VmsTagMapBase<T> {

    public DetachVmFromTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        for (Guid vmGuid : getVmsList()) {
            if (getTagId() != null && tagDao.getTagVmByTagIdAndByVmId(getTagId(), vmGuid) != null) {
                VM vm = vmDao.get(vmGuid);
                if (vm != null) {
                    appendCustomCommaSeparatedValue("VmsNames", vm.getName());
                }
                tagDao.detachVmFromTag(getTagId(), vmGuid);
                setSucceeded(true);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACH_VM_FROM_TAG : AuditLogType.USER_DETACH_VM_FROM_TAG_FAILED;
    }
}
