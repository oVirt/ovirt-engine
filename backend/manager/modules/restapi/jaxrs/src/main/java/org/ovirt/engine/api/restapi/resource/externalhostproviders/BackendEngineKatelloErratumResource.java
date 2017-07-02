package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErratumResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendEngineKatelloErratumResource extends AbstractBackendSubResource<KatelloErratum, Erratum> implements KatelloErratumResource {

    public BackendEngineKatelloErratumResource(String id) {
        super(id, KatelloErratum.class, Erratum.class);
    }

    @Override
    public KatelloErratum get() {
        return performGet(QueryType.GetErratumByIdForEngine, new NameQueryParameters(hex2string(id)));
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier isn't a UUID.
        return null;
    }
}
