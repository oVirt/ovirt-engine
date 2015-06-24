package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import java.util.List;

import org.ovirt.engine.api.model.KatelloErrata;
import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErratumResource;
import org.ovirt.engine.api.resource.externalhostproviders.SystemKatelloErrataResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.SingleEntityResource;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendSystemKatelloErrataResource extends AbstractBackendCollectionResource<KatelloErratum, Erratum> implements SystemKatelloErrataResource {

    public BackendSystemKatelloErrataResource() {
        super(KatelloErratum.class, Erratum.class);
    }

    @Override
    public KatelloErrata list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetErrataForSystem, new VdcQueryParametersBase()));
    }

    private KatelloErrata mapCollection(List<Erratum> entities) {
        KatelloErrata collection = new KatelloErrata();
        for (org.ovirt.engine.core.common.businessentities.Erratum entity : entities) {
            collection.getKatelloErrata().add(addLinks(populate(map(entity), entity)));
        }

        return collection;
    }

    @SingleEntityResource
    @Override
    public KatelloErratumResource getKatelloErratumSubResource(String id) {
        return inject(new BackendSystemKatelloErratumResource(id));
    }
}
