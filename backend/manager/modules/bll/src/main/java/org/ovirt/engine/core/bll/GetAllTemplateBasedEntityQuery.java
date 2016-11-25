package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.VmTemplateDao;

public abstract class GetAllTemplateBasedEntityQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmTemplateHandler vmTemplateHandler;

    private final VmEntityType entityType;

    public GetAllTemplateBasedEntityQuery(P parameters, VmEntityType entityType) {
        super(parameters);

        this.entityType = entityType;
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> retval = vmTemplateDao.getAll(getUserID(), getParameters().isFiltered(), entityType);
        for (VmTemplate template : retval) {
            vmTemplateHandler.updateDisksFromDb(template);
        }
        getQueryReturnValue().setReturnValue(retval);
    }
}
