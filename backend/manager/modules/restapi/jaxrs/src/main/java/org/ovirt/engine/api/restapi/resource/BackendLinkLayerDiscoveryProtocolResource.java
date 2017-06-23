package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.LinkLayerDiscoveryProtocolElement;
import org.ovirt.engine.api.model.LinkLayerDiscoveryProtocolElements;
import org.ovirt.engine.api.resource.LinkLayerDiscoveryProtocolResource;
import org.ovirt.engine.core.common.businessentities.network.Tlv;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendLinkLayerDiscoveryProtocolResource
        extends AbstractBackendCollectionResource<LinkLayerDiscoveryProtocolElement, Tlv>
        implements LinkLayerDiscoveryProtocolResource {

    private Guid nicId;

    public BackendLinkLayerDiscoveryProtocolResource(Guid nicId) {
        super(LinkLayerDiscoveryProtocolElement.class, Tlv.class);
        this.nicId = nicId;
    }

    @Override
    public LinkLayerDiscoveryProtocolElements list() {

        IdQueryParameters queryParameters = new IdQueryParameters(nicId);

        List<Tlv> tlvs = getBackendCollection(QueryType.GetTlvsByHostNicId, queryParameters);

        LinkLayerDiscoveryProtocolElements linkLayerDiscoveryProtocolElements =
                new LinkLayerDiscoveryProtocolElements();

        if (tlvs != null) {
            for (Tlv entity : tlvs) {
                linkLayerDiscoveryProtocolElements.getLinkLayerDiscoveryProtocolElements().add(map(entity));
            }
        }
        return linkLayerDiscoveryProtocolElements;
    }
}

