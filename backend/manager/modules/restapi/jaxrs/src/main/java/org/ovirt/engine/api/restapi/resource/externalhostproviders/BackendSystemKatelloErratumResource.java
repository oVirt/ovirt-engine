package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErratumResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendSystemKatelloErratumResource extends AbstractBackendSubResource<KatelloErratum, Erratum> implements KatelloErratumResource {

    public BackendSystemKatelloErratumResource(String id) {
        super(id, KatelloErratum.class, Erratum.class);
    }

    @Override
    public KatelloErratum get() {
        return performGet(VdcQueryType.GetErratumByIdForSystem, new NameQueryParameters(hex2string(id)));
    }

    @Override
    protected KatelloErratum doPopulate(KatelloErratum model, Erratum entity) {
        return model;
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier isn't a UUID.
        return null;
    }
}
