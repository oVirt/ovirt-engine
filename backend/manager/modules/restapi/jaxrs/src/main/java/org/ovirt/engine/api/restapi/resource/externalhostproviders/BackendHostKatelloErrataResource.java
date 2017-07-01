package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.KatelloErrata;
import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErrataResource;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErratumResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendHostKatelloErrataResource extends AbstractBackendCollectionResource<KatelloErratum, Erratum> implements KatelloErrataResource {

    private String hostId;

    public BackendHostKatelloErrataResource(String hostId) {
        super(KatelloErratum.class, Erratum.class);
        this.hostId = hostId;
    }

    @Override
    public KatelloErrata list() {
        ErrataData errataData = null;

        try {
            QueryReturnValue returnValue =
                    runQuery(QueryType.GetErrataForHost, new GetErrataCountsParameters(asGuid(hostId)));
            if (!returnValue.getSucceeded()) {
                backendFailure(returnValue.getExceptionString());
            }

            errataData = returnValue.getReturnValue();
        } catch (Exception e) {
            handleError(e, false);
        }

        if (errataData == null) {
            return new KatelloErrata();
        }

        return mapCollection(errataData.getErrata());
    }

    private KatelloErrata mapCollection(List<Erratum> entities) {
        KatelloErrata collection = new KatelloErrata();
        for (org.ovirt.engine.core.common.businessentities.Erratum entity : entities) {
            collection.getKatelloErrata().add(addLinks(populate(map(entity), entity), Host.class));
        }

        return collection;
    }

    @Override
    public KatelloErratumResource getKatelloErratumResource(String id) {
        return inject(new BackendHostKatelloErratumResource(id, hostId));
    }

    @Override
    protected KatelloErratum addParents(KatelloErratum erratum) {
        Host host = new Host();
        host.setId(hostId);
        erratum.setHost(host);
        return super.addParents(erratum);
    }
}
