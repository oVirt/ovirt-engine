package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesDisksQuery<P extends GetVmTemplatesDisksParameters> extends QueriesCommandBase<P> {
    public GetVmTemplatesDisksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getTemplateDisks());
    }

    protected List<DiskImage> getTemplateDisks() {
        return DbFacade.getInstance().getDiskImageDAO().getAllForVm(getParameters().getId());
    }
}
