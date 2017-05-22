package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class IsVmTemlateWithSameNameExistQuery<P extends NameQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmTemplateDao vmTemplateDao;

    public IsVmTemlateWithSameNameExistQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                isVmTemlateWithSameNameExist(getParameters().getName(),
                        getParameters().getDatacenterId()));
    }

    public boolean isVmTemlateWithSameNameExist(String name, Guid datacenterId) {
        return vmTemplateDao.getByName(name, datacenterId, null, false) != null;
    }
}
