package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachVdsToTagParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DetachVdsFromTagCommand<T extends AttachVdsToTagParameters> extends VdsTagMapBase<T> {

    public DetachVdsFromTagCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        for (Guid vdsId : getVdsList()) {
            if (getTagId() != null && DbFacade.getInstance().getTagDAO().getTagVdsByTagIdAndByVdsId(getTagId(), vdsId) != null) {
                VDS vds = DbFacade.getInstance().getVdsDAO().get(vdsId);
                if (vds != null) {
                    AppendCustomValue("VdsNames", vds.getvds_name(), ", ");
                }
                DbFacade.getInstance().getTagDAO().detachVdsFromTag(getTagId(), vdsId);
                setSucceeded(true);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACH_VDS_FROM_TAG : AuditLogType.USER_DETACH_VDS_FROM_TAG_FAILED;
    }
}
