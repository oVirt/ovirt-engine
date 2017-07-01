package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import java.util.List;

import org.ovirt.engine.api.model.KatelloErrata;
import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErrataResource;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErratumResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.GetErrataCountsParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendVmKatelloErrataResource
        extends AbstractBackendCollectionResource<KatelloErratum, Erratum>
        implements KatelloErrataResource {

    private String vmId;

    public BackendVmKatelloErrataResource(String vmId) {
        super(KatelloErratum.class, Erratum.class);
        this.vmId = vmId;
    }

    @Override
    public KatelloErrata list() {
        ErrataData errataData = null;

        try {
            QueryReturnValue returnValue =
                    runQuery(QueryType.GetErrataForVm, new GetErrataCountsParameters(asGuid(vmId)));
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
            collection.getKatelloErrata().add(addLinks(populate(map(entity), entity), Vm.class));
        }

        return collection;
    }

    @Override
    public KatelloErratumResource getKatelloErratumResource(String id) {
        return inject(new BackendVmKatelloErratumResource(id, vmId));
    }

    @Override
    protected KatelloErratum addParents(KatelloErratum erratum) {
        Vm vm = new Vm();
        vm.setId(vmId);
        erratum.setVm(vm);
        return super.addParents(erratum);
    }
}
