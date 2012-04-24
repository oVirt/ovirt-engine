package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Disk;
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

    protected List<Disk> getTemplateDisks() {
        return DbFacade.getInstance()
                .getDiskDao()
                .getAllForVm(getParameters().getId(), getUserID(), getParameters().isFiltered());
    }
}
