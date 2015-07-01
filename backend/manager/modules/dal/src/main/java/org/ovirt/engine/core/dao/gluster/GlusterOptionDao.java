package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.dao.MassOperationsDao;

/**
 * Interface for DB operations on Gluster Options.
 */
public interface GlusterOptionDao extends Dao, MassOperationsDao<GlusterVolumeOptionEntity, Guid> {
    public void save(GlusterVolumeOptionEntity option);

    public GlusterVolumeOptionEntity getById(Guid id);

    public List<GlusterVolumeOptionEntity> getOptionsOfVolume(Guid volumeId);

    public void updateVolumeOption(Guid optionId, String optionValue);

    public void removeVolumeOption(Guid optionId);
}
