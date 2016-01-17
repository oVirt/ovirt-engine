package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AttachVmsToTagCommand<T extends AttachEntityToTagParameters> extends VmsTagMapBase<T> {

    public AttachVmsToTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid vmGuid : getVmsList()) {
                VM vm = DbFacade.getInstance().getVmDao().get(vmGuid);
                if (DbFacade.getInstance().getTagDao().getTagVmByTagIdAndByVmId(getTagId(), vmGuid) == null) {
                    if (vm != null) {
                        appendCustomValue("VmsNames", vm.getName(), ", ");
                    }
                    TagsVmMap map = new TagsVmMap(getTagId(), vmGuid);
                    DbFacade.getInstance().getTagDao().attachVmToTag(map);
                    noActionDone = false;
                } else {
                    if (vm != null) {
                        appendCustomValue("VmsNamesExists", vm.getName(), ", ");
                    }
                }
            }
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (noActionDone) {
            return AuditLogType.USER_ATTACH_TAG_TO_VM_EXISTS;
        }
        return getSucceeded() ? AuditLogType.USER_ATTACH_TAG_TO_VM : AuditLogType.USER_ATTACH_TAG_TO_VM_FAILED;
    }
}
