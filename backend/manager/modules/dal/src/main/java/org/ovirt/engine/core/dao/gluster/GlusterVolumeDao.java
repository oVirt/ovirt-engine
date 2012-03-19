package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOption;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DAO;

/**
 * Interface for DB operations on Gluster Volumes.
 */
public interface GlusterVolumeDao extends DAO {

    public void save(GlusterVolumeEntity volume);

    public GlusterVolumeEntity getById(Guid id);

    public GlusterVolumeEntity getByName(Guid clusterId, String volName);

    public List<GlusterVolumeEntity> getByClusterId(Guid clusterId);

    public List<GlusterVolumeEntity> getAllWithQuery(String query);

    public void remove(Guid id);

    public void removeByName(Guid clusterId, String volName);

    public void updateVolumeStatus(Guid volumeId, GlusterVolumeStatus status);

    public void updateVolumeStatusByName(Guid clusterId, String volumeName, GlusterVolumeStatus status);

    public void addBrickToVolume(GlusterBrickEntity brick);

    public void removeBrickFromVolume(GlusterBrickEntity brick);

    public void replaceVolumeBrick(GlusterBrickEntity oldBrick, GlusterBrickEntity newBrick);

    public void updateBrickStatus(GlusterBrickEntity brick);

    public void addVolumeOption(GlusterVolumeOption option);

    public void updateVolumeOption(GlusterVolumeOption option);

    public void removeVolumeOption(GlusterVolumeOption option);

    public void addAccessProtocol(Guid volumeId, AccessProtocol protocol);

    public void removeAccessProtocol(Guid volumeId, AccessProtocol protocol);
}
