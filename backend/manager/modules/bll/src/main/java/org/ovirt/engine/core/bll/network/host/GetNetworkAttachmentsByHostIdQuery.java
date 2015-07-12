package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;

public class GetNetworkAttachmentsByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private ReportedConfigurationsFiller reportedConfigurationsFiller;

    public GetNetworkAttachmentsByHostIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid hostId = getParameters().getId();

        List<NetworkAttachment> networkAttachments = networkAttachmentDao.getAllForHost(hostId);
        reportedConfigurationsFiller.fillReportedConfigurations(networkAttachments, hostId);
        getQueryReturnValue().setReturnValue(networkAttachments);
    }
}
