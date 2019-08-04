package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetUnregisteredEntityQueryParameters;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetUnregisteredVmQuery<P extends GetUnregisteredEntityQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private OvfHelper ovfHelper;
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;

    public GetUnregisteredVmQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<OvfEntityData> entityList =
                unregisteredOVFDataDao.getByEntityIdAndStorageDomain(getParameters().getEntityId(),
                        getParameters().getStorageDomainId());

        VM vm = null;
        if (!entityList.isEmpty()) {
            // We should get only one entity, since we fetched the entity with a specific Storage Domain
            OvfEntityData ovfEntityData = entityList.get(0);
            try {
                vm = ovfHelper.readVmFromOvf(ovfEntityData.getOvfData()).getVm();
            } catch (OvfReaderException e) {
                log.debug("failed to parse a given ovf configuration: \n" + ovfEntityData.getOvfData(), e);
                getQueryReturnValue().setExceptionString("failed to parse a given ovf configuration "
                        + e.getMessage());
            }
        }

        getQueryReturnValue().setReturnValue(vm);
    }
}
