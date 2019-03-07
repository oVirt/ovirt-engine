package org.ovirt.engine.core.bll.network.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

/**
 * A query to retrieve all VmTemplate-Network Interface pairs that the given Network is attached to.
 */
public class GetVmTemplatesAndNetworkInterfacesByNetworkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    public GetVmTemplatesAndNetworkInterfacesByNetworkIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> vmTemplateList = vmTemplateDao.getAllForNetwork(getParameters().getId());
        List<VmNetworkInterface> vmNetworkInterfaceList =
                vmNetworkInterfaceDao.getAllForTemplatesByNetwork(getParameters().getId());

        final Map<Guid, VmTemplate> vmTemplatesById = Entities.businessEntitiesById(vmTemplateList);

        List<PairQueryable<VmNetworkInterface, VmTemplate>> vmInterfaceVmPairs = new ArrayList<>();
        for (VmNetworkInterface vmNetworkInterface : vmNetworkInterfaceList) {
            vmInterfaceVmPairs.add(new PairQueryable<>(vmNetworkInterface,
                    vmTemplatesById.get(vmNetworkInterface.getVmId())));
        }

        getQueryReturnValue().setReturnValue(vmInterfaceVmPairs);
    }
}
