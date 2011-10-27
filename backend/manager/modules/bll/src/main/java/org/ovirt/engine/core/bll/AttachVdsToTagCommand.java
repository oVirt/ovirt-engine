package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachVdsToTagParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.tags_vds_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AttachVdsToTagCommand<T extends AttachVdsToTagParameters> extends VdsTagMapBase<T> {

    public AttachVdsToTagCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        tags_vds_map map;
        if (getTagId() != null) {
            for (Guid vdsId : getVdsList()) {
                VDS vds = DbFacade.getInstance().getVdsDAO().get(vdsId);
                if (DbFacade.getInstance().getTagDAO().getTagVdsByTagIdAndByVdsId(getTagId(), vdsId) == null) {
                    if (vds != null) {
                        AppendCustomValue("VdsNames", vds.getvds_name(), ", ");
                    }
                    map = new tags_vds_map(getTagId(), vdsId);
                    DbFacade.getInstance().getTagDAO().attachVdsToTag(map);
                    noActionDone = false;
                } else {
                    if (vds != null) {
                        AppendCustomValue("VdsNamesExists", vds.getvds_name(), ", ");
                    }
                }
            }
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (noActionDone) {
            return AuditLogType.USER_ATTACH_TAG_TO_VDS_EXISTS;
        }
        return getSucceeded() ? AuditLogType.USER_ATTACH_TAG_TO_VDS : AuditLogType.USER_ATTACH_TAG_TO_VDS_FAILED;
    }
}
