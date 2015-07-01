package org.ovirt.engine.core.bll.numa.vm;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmNumaNodesByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetVmNumaNodesByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmNumaNode> numaNodes = getDbFacade().getVmNumaNodeDao()
                .getAllVmNumaNodeByVmId(getParameters().getId());
        getQueryReturnValue().setReturnValue(numaNodes);
    }

}
