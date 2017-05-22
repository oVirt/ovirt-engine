package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;

public class GetNetworkAttachmentByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private ReportedConfigurationsFiller reportedConfigurationsFiller;

    public GetNetworkAttachmentByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid networkAttachmentId = getParameters().getId();
        NetworkAttachment networkAttachment = networkAttachmentDao.get(networkAttachmentId);
        if (networkAttachment != null) {
            fillReportedConfigurations(networkAttachment);
        }

        getQueryReturnValue().setReturnValue(networkAttachment);
    }

    private void fillReportedConfigurations(NetworkAttachment networkAttachment) {
        VdsNetworkInterface vdsNetworkInterface = interfaceDao.get(networkAttachment.getNicId());
        Guid hostId = vdsNetworkInterface.getVdsId();

        reportedConfigurationsFiller.fillReportedConfiguration(networkAttachment, hostId);
    }
}
