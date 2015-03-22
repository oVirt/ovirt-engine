package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

import java.util.ArrayList;
import java.util.List;

public class GetCinderVolumeTypesByStorageDomainIdQuery<P extends IdQueryParameters> extends CinderQueryBase<P> {

    public GetCinderVolumeTypesByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<CinderVolumeType> volumeTypes = getVolumeProviderProxy().getVolumeTypes();
        getQueryReturnValue().setReturnValue(new ArrayList<>(volumeTypes));
    }
}
