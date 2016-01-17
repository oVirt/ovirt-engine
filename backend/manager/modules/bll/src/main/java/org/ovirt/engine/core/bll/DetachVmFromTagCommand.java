package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DetachVmFromTagCommand<T extends AttachEntityToTagParameters> extends VmsTagMapBase<T> {

    public DetachVmFromTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        for (Guid vmGuid : getVmsList()) {
            if (getTagId() != null
                    && DbFacade.getInstance().getTagDao().getTagVmByTagIdAndByVmId(getTagId(), vmGuid) != null) {
                VM vm = DbFacade.getInstance().getVmDao().get(vmGuid);
                if (vm != null) {
                    appendCustomValue("VmsNames", vm.getName(), ", ");
                }
                DbFacade.getInstance().getTagDao().detachVmFromTag(getTagId(), vmGuid);
                setSucceeded(true);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACH_VM_FROM_TAG : AuditLogType.USER_DETACH_VM_FROM_TAG_FAILED;
    }
}
