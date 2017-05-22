package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;

public class GetNetworkAttachmentsByHostNicIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private ReportedConfigurationsFiller reportedConfigurationsFiller;

    public GetNetworkAttachmentsByHostNicIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid nicId = getParameters().getId();
        List<NetworkAttachment> networkAttachments = networkAttachmentDao.getAllForNic(nicId);

        if (!networkAttachments.isEmpty()) {
            fillReportedConfigurations(nicId, networkAttachments);
        }
        getQueryReturnValue().setReturnValue(networkAttachments);
    }

    private void fillReportedConfigurations(Guid nicId, List<NetworkAttachment> networkAttachments) {
        VdsNetworkInterface nic = interfaceDao.get(nicId);

        if (nic != null) {
            reportedConfigurationsFiller.fillReportedConfigurations(networkAttachments, nic.getVdsId());
        }
    }
}
