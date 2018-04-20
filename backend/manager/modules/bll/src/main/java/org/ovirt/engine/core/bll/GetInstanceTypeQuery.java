package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetInstanceTypeQuery<P extends GetVmTemplateParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmTemplateDao vmTemplateDao;

    public GetInstanceTypeQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        InstanceType instance;
        GetVmTemplateParameters params = getParameters();
        if (params.getName() != null) {
            instance = vmTemplateDao.getInstanceTypeByName(params.getName(), getUserID(), getParameters().isFiltered());
        } else {
            instance = vmTemplateDao.getInstanceType(getParameters().getId(), getUserID(), getParameters().isFiltered());
        }
        getQueryReturnValue().setReturnValue(instance);
    }

}
