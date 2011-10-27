package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesDisksQuery<P extends GetVmTemplatesDisksParameters> extends QueriesCommandBase<P> {
    public GetVmTemplatesDisksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(GetTemplateDisks());
    }

    protected java.util.ArrayList<DiskImage> GetTemplateDisks() {
        List<DiskImageTemplate> templateImages = DbFacade.getInstance().getDiskImageTemplateDAO().getAllByVmTemplate(
                getParameters().getId());
        java.util.ArrayList<DiskImage> templateDisks = new java.util.ArrayList<DiskImage>();
        for (DiskImageTemplate diTemplate : templateImages) {
            DiskImage templateDisk =
                    DbFacade.getInstance().getDiskImageDAO().getSnapshotById(diTemplate.getId());
            if (templateDisk != null) {
                templateDisks.add(templateDisk);
            }
        }
        return templateDisks;
    }
}
