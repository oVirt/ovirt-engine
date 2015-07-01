package org.ovirt.engine.core.bll.numa.host;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVdsNumaNodesByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetVdsNumaNodesByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
       List<VdsNumaNode> numaNodes = getDbFacade().getVdsNumaNodeDao()
                .getAllVdsNumaNodeByVdsId(getParameters().getId());
        getQueryReturnValue().setReturnValue(numaNodes);
    }

}
