package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code ImageDao} defines a type for performing CRUD operations on instances of {@link Image}.
 */
public interface ImageDao extends GenericDao<Image, Guid>, StatusAwareDao<Guid, ImageStatus> {
    void updateStatusOfImagesByImageGroupId(Guid imageGroupId, ImageStatus status);

    void updateImageVmSnapshotId(Guid id, Guid vmSnapshotId);

    void updateImageSize(Guid id, long size);

    Integer getMaxSequenceNumber(Guid imageGroupId);
}
