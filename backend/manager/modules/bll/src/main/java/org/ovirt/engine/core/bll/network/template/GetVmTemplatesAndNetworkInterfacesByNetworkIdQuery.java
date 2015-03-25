package org.ovirt.engine.core.bll.network.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;

/**
 * A query to retrieve all VmTemplate-Network Interface pairs that the given Network is attached to.
 */
public class GetVmTemplatesAndNetworkInterfacesByNetworkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmTemplatesAndNetworkInterfacesByNetworkIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> vmTemplateList = getDbFacade().getVmTemplateDao()
                .getAllForNetwork(getParameters().getId());
        List<VmNetworkInterface> vmNetworkInterfaceList = getDbFacade().getVmNetworkInterfaceDao()
                .getAllForTemplatesByNetwork(getParameters().getId());

        final Map<Guid, VmTemplate> vmTemplatesById = Entities.businessEntitiesById(vmTemplateList);

        List<PairQueryable<VmNetworkInterface, VmTemplate>> vmInterfaceVmPairs = new ArrayList<>();
        for (VmNetworkInterface vmNetworkInterface : vmNetworkInterfaceList) {
            vmInterfaceVmPairs.add(new PairQueryable<>(vmNetworkInterface,
                    vmTemplatesById.get(vmNetworkInterface.getVmTemplateId())));
        }

        getQueryReturnValue().setReturnValue(vmInterfaceVmPairs);
    }
}
