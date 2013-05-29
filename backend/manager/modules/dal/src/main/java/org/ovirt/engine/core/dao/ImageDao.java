package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.Image;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>ImageDAO</code> defines a type for performing CRUD operations on instances of {@link Image}.
 */
public interface ImageDao extends GenericDao<Image, Guid>, StatusAwareDao<Guid, ImageStatus> {
    void updateQuotaForImageAndSnapshots(Guid imageGroupId, Guid quotaId);

    public void updateImageVmSnapshotId(Guid id, Guid vmSnapshotId);
}
