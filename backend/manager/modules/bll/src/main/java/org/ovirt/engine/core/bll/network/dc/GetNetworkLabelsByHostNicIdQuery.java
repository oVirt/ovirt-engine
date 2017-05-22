package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public class GetNetworkLabelsByHostNicIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private InterfaceDao interfaceDao;

    public GetNetworkLabelsByHostNicIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VdsNetworkInterface nic = interfaceDao.get(getParameters().getId());
        getQueryReturnValue().setReturnValue(nic == null || !NetworkUtils.isLabeled(nic) ? Collections.<NetworkLabel> emptyList()
                : convertToNetworkLabels(nic.getLabels()));
    }

    private List<NetworkLabel> convertToNetworkLabels(Set<String> labels) {
        List<NetworkLabel> result = new ArrayList<>(labels.size());
        for (String label : labels) {
            result.add(new NetworkLabel(label));
        }

        return result;
    }
}
