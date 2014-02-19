package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

import java.util.List;

public abstract class GetAllTemplateBasedEntityQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    private final VmEntityType entityType;

    public GetAllTemplateBasedEntityQuery(P parameters, VmEntityType entityType) {
        super(parameters);

        this.entityType = entityType;
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> retval =
                getDbFacade().getVmTemplateDao().getAll(getUserID(), getParameters().isFiltered(), entityType);
        for (VmTemplate template : retval) {
            VmTemplateHandler.updateDisksFromDb(template);
        }
        getQueryReturnValue().setReturnValue(retval);
    }
}
