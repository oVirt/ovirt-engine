package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.compat.Guid;

public interface ImageTransferDao extends GenericDao<ImageTransfer, Guid>, SearchDao<ImageTransfer> {
    /**
     * Retrieves an ImageTransfer entity based on its disk id
     *
     * @return ImageTransfer entity
     */
    ImageTransfer getByDiskId(Guid diskId);
}
