package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.NetworkLabelsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicLabelsResource
    extends AbstractBaseHostNicLabelsResource
    implements NetworkLabelsResource {

    private Guid nicId;

    protected BackendHostNicLabelsResource(Guid nicId, String hostId) {
        super(nicId, hostId);

        this.nicId = nicId;
    }

    protected List<NetworkLabel> getHostNicLabels(Guid hostNicId) {
        return getBackendCollection(QueryType.GetNetworkLabelsByHostNicId, new IdQueryParameters(hostNicId));
    }

    @Override
    protected Response performCreate(String labelId) {
        return performCreate(ActionType.LabelNic,
                new LabelNicParameters(nicId, labelId),
                new NetworkLabelIdResolver(nicId));
    }

    @Override
    protected AbstractBaseHostNicLabelResource createSingularResource(String labelId) {
        return new BackendHostNicLabelResource(labelId, this);
    }
}
