package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.TagsVdsMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.dao.VdsDao;

public class AttachVdsToTagCommand<T extends AttachEntityToTagParameters> extends VdsTagMapBase<T> {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private TagDao tagDao;

    public AttachVdsToTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        TagsVdsMap map;
        if (getTagId() != null) {
            for (Guid vdsId : getVdsList()) {
                VDS vds = vdsDao.get(vdsId);
                if (tagDao.getTagVdsByTagIdAndByVdsId(getTagId(), vdsId) == null) {
                    if (vds != null) {
                        appendCustomCommaSeparatedValue("VdsNames", vds.getName());
                    }
                    map = new TagsVdsMap(getTagId(), vdsId);
                    tagDao.attachVdsToTag(map);
                    noActionDone = false;
                } else {
                    if (vds != null) {
                        appendCustomCommaSeparatedValue("VdsNamesExists", vds.getName());
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
