package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class DoesStorageDomainContainEntityWithDisksOnMultipleSDsQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;
    @Inject
    private VmTemplateDao vmTemplateDao;

    public DoesStorageDomainContainEntityWithDisksOnMultipleSDsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vms = vmDao.getAllVMsWithDisksOnOtherStorageDomain(getParameters().getId());
        if (vms != null && !vms.isEmpty()) {
            setReturnValue(true);
            return;
        }

        List<VmTemplate> templates =
                vmTemplateDao.getAllTemplatesWithDisksOnOtherStorageDomain(getParameters().getId());
        if (templates != null && !templates.isEmpty()) {
            setReturnValue(true);
            return;
        }
        setReturnValue(false);
    }
}
