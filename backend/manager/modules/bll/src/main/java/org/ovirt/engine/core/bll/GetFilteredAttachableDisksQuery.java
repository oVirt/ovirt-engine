package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.GetFilteredAttachableDisksParameters;

public class GetFilteredAttachableDisksQuery<P extends GetFilteredAttachableDisksParameters> extends BaseGetAttachableDisksQuery<P> {

    public GetFilteredAttachableDisksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<Disk> filterDisks(List<Disk> diskList) {
        return doFilter(diskList, getParameters().getOs(), getParameters().getVdsGroupCompatibilityVersion());
    }

}
