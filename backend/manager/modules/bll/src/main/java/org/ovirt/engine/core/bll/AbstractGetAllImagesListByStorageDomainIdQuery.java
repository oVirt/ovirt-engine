package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * An extension of the {@link AbstractGetAllImagesListQuery} class that handles getting images by Storage Domain ID
 */
public abstract class AbstractGetAllImagesListByStorageDomainIdQuery<P extends GetAllIsoImagesListParameters> extends AbstractGetAllImagesListQuery<P> {

    public AbstractGetAllImagesListByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    /**
     * @return The storage domain to get the ISO for
     */
    @Override
    protected Guid getStorageDomainId() {
        return getParameters().getStorageDomainId();
    }
}

