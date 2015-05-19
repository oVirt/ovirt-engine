package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDAO;

public class IsVmTemlateWithSameNameExistQuery<P extends NameQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmTemplateDAO vmTemplateDao;

    public IsVmTemlateWithSameNameExistQuery(P parameters) {
        super(parameters);
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
