package org.ovirt.engine.core.dao;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.image_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.stateless_vm_image_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.images.DiskImageDAOHibernateImpl;
import org.ovirt.engine.core.dao.images.DiskImageDynamicDAOHibernateImpl;
import org.ovirt.engine.core.dao.images.DiskImageTemplateDAOHibernateImpl;
import org.ovirt.engine.core.dao.images.ImageVmMapDAOHibernateImpl;
import org.ovirt.engine.core.dao.images.ImageVmPoolMapDAOHibernateImpl;
import org.ovirt.engine.core.dao.images.StatelessImageVmMapDAOHibernateImpl;

/**
 * <code>DiskImageDAOWrapperImpl</code> provides an implementation of {@link DiskImageDAO} that wraps underlying
 * Hibernate DAOs.
 *
 */
public class DiskImageDAOWrapperImpl extends BaseDAOWrapperImpl implements DiskImageDAO {
    private final DiskImageDAOHibernateImpl imageDAO = new DiskImageDAOHibernateImpl();
    private final DiskImageDynamicDAOHibernateImpl dynamicDAO = new DiskImageDynamicDAOHibernateImpl();
    private final DiskImageTemplateDAOHibernateImpl templateDAO = new DiskImageTemplateDAOHibernateImpl();
    private final ImageVmMapDAOHibernateImpl imageVmMapDAO = new ImageVmMapDAOHibernateImpl();
    private final ImageVmPoolMapDAOHibernateImpl imageVmPoolMapDAO = new ImageVmPoolMapDAOHibernateImpl();
    private final StatelessImageVmMapDAOHibernateImpl statelessImageVmMapDAO =
            new StatelessImageVmMapDAOHibernateImpl();

    @Override
    public void setSession(Session session) {
        super.setSession(session);

        imageDAO.setSession(session);
        dynamicDAO.setSession(session);
        templateDAO.setSession(session);
        imageVmMapDAO.setSession(session);
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
    public List<DiskImage> getAllForVm(Guid id) {
        return imageDAO.getAllForVm(id);
    }

    @Override
    public List<DiskImage> getAllForVm(Guid id, Guid userID, boolean isFiltered) {
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
    public void save(DiskImage image) {
        imageDAO.save(image);
    }

    @Override
    public void update(DiskImage image) {
        imageDAO.update(image);
    }

    @Override
    public void remove(Guid id) {
        imageDAO.remove(id);
    }

    @Override
    public void removeAllForVmId(Guid id) {
        imageDAO.removeAllForVmId(id);
    }

    @Override
    public image_vm_pool_map getImageVmPoolMapByImageId(Guid imageId) {
        return imageVmPoolMapDAO.findOneByCriteria(Restrictions.eq("imageId", imageId));
    }

    @Override
    public void addImageVmPoolMap(image_vm_pool_map map) {
        imageVmPoolMapDAO.save(map);
    }

    @Override
    public void removeImageVmPoolMap(Guid imageId) {
        imageVmPoolMapDAO.remove(imageId);
    }

    @Override
    public List<image_vm_pool_map> getImageVmPoolMapByVmId(Guid vmId) {
        return imageVmPoolMapDAO.findByCriteria(Restrictions.eq("vmId", vmId));
    }

    @Override
    public stateless_vm_image_map getStatelessVmImageMapForImageId(Guid imageId) {
        return statelessImageVmMapDAO.get(imageId);
    }

    @Override
    public void addStatelessVmImageMap(stateless_vm_image_map map) {
        statelessImageVmMapDAO.save(map);
    }

    @Override
    public void removeStatelessVmImageMap(Guid imageId) {
        statelessImageVmMapDAO.remove(imageId);
    }

    @Override
    public List<stateless_vm_image_map> getAllStatelessVmImageMapsForVm(Guid vmId) {
        return statelessImageVmMapDAO.findByCriteria(Restrictions.eq("vmId", vmId));
    }

    @Override
    public DiskImage getAncestor(Guid id) {
        throw new NotImplementedException();
    }
}
