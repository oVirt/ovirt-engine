package org.ovirt.engine.core.bll.numa.vm;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmNumaNodeDao;

public class GetVmNumaNodesByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmNumaNodeDao vmNumaNodeDao;

    public GetVmNumaNodesByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmNumaNode> numaNodes = vmNumaNodeDao.getAllVmNumaNodeByVmId(getParameters().getId());
        getQueryReturnValue().setReturnValue(numaNodes);
    }

}
