package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetCinderVolumeTypesByStorageDomainIdQuery<P extends IdQueryParameters> extends CinderQueryBase<P> {

    public GetCinderVolumeTypesByStorageDomainIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<CinderVolumeType> volumeTypes = getVolumeProviderProxy().getVolumeTypes();
        getQueryReturnValue().setReturnValue(new ArrayList<>(volumeTypes));
    }
}
