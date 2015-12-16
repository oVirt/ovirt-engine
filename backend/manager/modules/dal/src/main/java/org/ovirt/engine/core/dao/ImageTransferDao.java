package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.compat.Guid;

public interface ImageTransferDao extends GenericDao<ImageTransfer, Guid> {
    /**
     * Retrieves an ImageUpload entity based on its disk id
     *
     * @return ImageUpload entity
     */
    ImageTransfer getByDiskId(Guid diskId);

    /**
     * Retrieves a list of all Image Upload command ids
     *
     * @returns A list of Guids
     */
    List<Guid> getAllIds();

}
