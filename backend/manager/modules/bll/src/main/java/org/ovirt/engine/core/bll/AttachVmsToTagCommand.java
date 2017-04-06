package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.TagsVmMap;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.dao.VmDao;

public class AttachVmsToTagCommand<T extends AttachEntityToTagParameters> extends VmsTagMapBase<T> {

    @Inject
    private TagDao tagDao;
    @Inject
    private VmDao vmDao;

    public AttachVmsToTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid vmGuid : getVmsList()) {
                VM vm = vmDao.get(vmGuid);
                if (tagDao.getTagVmByTagIdAndByVmId(getTagId(), vmGuid) == null) {
                    if (vm != null) {
                        appendCustomCommaSeparatedValue("VmsNames", vm.getName());
                    }
                    TagsVmMap map = new TagsVmMap(getTagId(), vmGuid);
                    tagDao.attachVmToTag(map);
                    noActionDone = false;
                } else {
                    if (vm != null) {
                        appendCustomCommaSeparatedValue("VmsNamesExists", vm.getName());
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
