package org.ovirt.engine.core.bll.storage.repoimage;

import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.compat.Guid;


public class GetImagesListQuery<P extends GetImagesListParameters> extends GetImagesListQueryBase<P> {

    public GetImagesListQuery(P parameters) {
        super(parameters);
    }

    /**
     * @return The storage domain to get the images from
     */
    @Override
    protected Guid getStorageDomainIdForQuery() {
        return getParameters().getStorageDomainId();
    }
}
