package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.queries.GetFilteredAttachableDisksParameters;

import java.util.List;

public class GetFilteredAttachableDisksQuery<P extends GetFilteredAttachableDisksParameters> extends BaseGetAttachableDisksQuery<P> {

    public GetFilteredAttachableDisksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<Disk> filterDisks(List<Disk> diskList) {
        return doFilter(diskList, getParameters().getOs(), getParameters().getVdsGroupCompatibilityVersion());
    }

}
