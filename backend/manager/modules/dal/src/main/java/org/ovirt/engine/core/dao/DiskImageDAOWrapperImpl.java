package org.ovirt.engine.core.dao;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Session;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.images.DiskImageDAOHibernateImpl;
import org.ovirt.engine.core.dao.images.DiskImageDynamicDAOHibernateImpl;

/**
 * <code>DiskImageDAOWrapperImpl</code> provides an implementation of {@link DiskImageDAO} that wraps underlying
 * Hibernate DAOs.
 *
 */
public class DiskImageDAOWrapperImpl extends BaseDAOWrapperImpl implements DiskImageDAO {
    private final DiskImageDAOHibernateImpl imageDAO = new DiskImageDAOHibernateImpl();
    private final DiskImageDynamicDAOHibernateImpl dynamicDAO = new DiskImageDynamicDAOHibernateImpl();

    @Override
    public void setSession(Session session) {
        super.setSession(session);

        imageDAO.setSession(session);
        dynamicDAO.setSession(session);
    }

    @Override
    public DiskImage get(Guid id) {
        return imageDAO.get(id);
    }

    @Override
    public DiskImage getSnapshotById(Guid id) {
        return imageDAO.get(id);
    }

    @Override
    public List<DiskImage> getAllForQuotaId(Guid quotaId) {
        throw new NotImplementedException("This method is not implemented for Hibernate yet");
    }

    @Override
    public List<DiskImage> getAllSnapshotsForParent(Guid id) {
        return imageDAO.getAllSnapshotsForParent(id);
    }

    @Override
    public List<DiskImage> getAllSnapshotsForStorageDomain(Guid id) {
        return imageDAO.getAllSnapshotsForStorageDomain(id);
    }

    @Override
    public List<DiskImage> getAllSnapshotsForVmSnapshot(Guid id) {
        return imageDAO.getAllSnapshotsForVmSnapshot(id);
    }

    @Override
    public List<DiskImage> getAllSnapshotsForImageGroup(Guid id) {
        return imageDAO.getAllSnapshotsForImageGroup(id);
    }

    @Override
    public List<DiskImage> getAll() {
        return imageDAO.getAll();
    }

    @Override
    public DiskImage getAncestor(Guid id) {
        throw new NotImplementedException();
    }

    @Override
    public List<DiskImage> getImagesWithNoDisk(Guid vmId) {
        throw new NotImplementedException();
    }
}
