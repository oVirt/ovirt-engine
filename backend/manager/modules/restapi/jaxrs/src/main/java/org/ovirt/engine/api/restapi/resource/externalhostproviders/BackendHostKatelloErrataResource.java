package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.KatelloErrata;
import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErrataResource;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErratumResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.SingleEntityResource;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostKatelloErrataResource extends AbstractBackendCollectionResource<KatelloErratum, Erratum> implements KatelloErrataResource {

    private String hostId;

    public BackendHostKatelloErrataResource(String hostId) {
        super(KatelloErratum.class, Erratum.class);
        this.hostId = hostId;
    }

    @Override
    public KatelloErrata list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetErrataForHost, new IdQueryParameters(asGuid(hostId))));
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
        return inject(new BackendHostKatelloErratumResource(id, hostId));
    }

    @Override
    protected Response performRemove(String id) {
        throw new NotImplementedException();
    }

    @Override
    protected KatelloErratum doPopulate(KatelloErratum model, Erratum entity) {
        return model;
    }

    @Override
    protected KatelloErratum addParents(KatelloErratum erratum) {
        Host host = new Host();
        host.setId(hostId);
        erratum.setHost(host);
        return super.addParents(erratum);
    }
}
