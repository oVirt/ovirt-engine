package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErratumResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.HostErratumQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostKatelloErratumResource extends AbstractBackendSubResource<KatelloErratum, Erratum> implements KatelloErratumResource {

    private String hostId;

    public BackendHostKatelloErratumResource(String id, String hostId) {
        super(id, KatelloErratum.class, Erratum.class);
        this.hostId = hostId;
    }

    @Override
    public KatelloErratum get() {
        return performGet(QueryType.GetErratumByIdForHost, new HostErratumQueryParameters(asGuid(hostId),
                hex2string(id)), Host.class);
    }

    @Override
    protected KatelloErratum addParents(KatelloErratum erratum) {
        Host host = new Host();
        host.setId(hostId);
        erratum.setHost(host);
        return super.addParents(erratum);
    }

    @Override
    protected KatelloErratum addLinks(KatelloErratum model,
            Class<? extends BaseResource> suggestedParent,
            String... subCollectionMembersToExclude) {
        return super.addLinks(model, Host.class);
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier isn't a UUID.
        return null;
    }
}
