package org.ovirt.engine.core.dao.images;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

/**
 * <code>DiskImageDAOHibernateImpl</code> performs persistence operations on instances of {@link DiskImage} using
 * Hibernate.
 *
 */
public class DiskImageDAOHibernateImpl extends BaseDAOHibernateImpl<DiskImage, Guid> {
    public DiskImageDAOHibernateImpl() {
        super(DiskImage.class);
    }

    @SuppressWarnings("unchecked")
    public List<DiskImage> getAllForVm(Guid id) {
        Query query = getSession().createQuery("select image from DiskImage image, " +
                "image_vm_map ivmap " +
                "where ivmap.id.imageId = image.id " +
                "and ivmap.id.vmId = :vm_id");

        query.setParameter("vm_id", id);

        return fillInDetails(query.list());
    }

    private List<DiskImage> fillInDetails(List<DiskImage> images) {
        for (DiskImage image : images) {
            Query query = getSession().createQuery("from image_vm_map where id.imageId = :image_id");

            query.setParameter("image_id", image.getImageId());
        }

        return images;
    }

    public List<DiskImage> getAllSnapshotsForParent(Guid id) {
        return fillInDetails(findByCriteria(Restrictions.eq("parentId", id)));
    }

    public List<DiskImage> getAllSnapshotsForStorageDomain(Guid id) {
        return fillInDetails(findByCriteria(Restrictions.eq("storage_id", id)));
    }

    public List<DiskImage> getAllSnapshotsForVmSnapshot(Guid id) {
        return fillInDetails(findByCriteria(Restrictions.eq("vm_snapshot_id", id)));
    }

    public List<DiskImage> getAllSnapshotsForImageGroup(Guid id) {
        return fillInDetails(findByCriteria(Restrictions.eq("image_group_id", id)));
    }

    public void removeAllForVmId(Guid id) {
        Query query = getSession().createQuery("delete from DiskImage i " +
                "where i.id = (select ivmap.id.imageId from image_vm_map ivmap where ivmap.id.vmId = :vm_id)");

        query.setParameter("vm_id", id);

        Transaction transaction = getSession().beginTransaction();

        query.executeUpdate();
        transaction.commit();
    }
}
