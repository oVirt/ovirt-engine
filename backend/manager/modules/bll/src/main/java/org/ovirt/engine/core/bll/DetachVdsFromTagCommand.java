package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;

public class DetachVdsFromTagCommand<T extends AttachEntityToTagParameters> extends VdsTagMapBase<T> {

    public DetachVdsFromTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        for (Guid vdsId : getVdsList()) {
            if (getTagId() != null && tagDao.getTagVdsByTagIdAndByVdsId(getTagId(), vdsId) != null) {
                VDS vds = vdsDao.get(vdsId);
                if (vds != null) {
                    appendCustomCommaSeparatedValue("VdsNames", vds.getName());
                }
                tagDao.detachVdsFromTag(getTagId(), vdsId);
                setSucceeded(true);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACH_VDS_FROM_TAG : AuditLogType.USER_DETACH_VDS_FROM_TAG_FAILED;
    }
}
