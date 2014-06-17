package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesDisksQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmTemplatesDisksQuery(P parameters) {
        super(parameters);
    }

    public GetVmTemplatesDisksQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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
