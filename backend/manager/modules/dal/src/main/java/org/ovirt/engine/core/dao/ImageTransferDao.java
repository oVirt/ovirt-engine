package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.compat.Guid;

public interface ImageTransferDao extends GenericDao<ImageTransfer, Guid>, SearchDao<ImageTransfer> {
    /**
     * Retrieves an ImageTransfer entity based on its disk id
     *
     * @return ImageTransfer entity
     */
    ImageTransfer getByDiskId(Guid diskId);

    /**
     * Retrieves an ImageTransfer entities based on vds id
     *
     * @return ImageTransfer entity
     */
    List<ImageTransfer> getByVdsId(Guid vdsId);

    /**
     *
     * @return ImageTransfer entity
     */
    ImageTransfer get(Guid diskId, Guid userId, boolean isFiltered);
}
