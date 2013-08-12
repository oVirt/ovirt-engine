package org.ovirt.engine.core.dao.gluster;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DAO;
import org.ovirt.engine.core.dao.MassOperationsDao;
import org.ovirt.engine.core.dao.SearchDAO;

/**
 * Interface for DB operations on Gluster Volumes.
 */
public interface GlusterVolumeDao extends DAO, SearchDAO<GlusterVolumeEntity>, MassOperationsDao<GlusterVolumeEntity, Guid> {

    public void save(GlusterVolumeEntity volume);

    public GlusterVolumeEntity getById(Guid id);

    public GlusterVolumeEntity getByName(Guid clusterId, String volName);

    public List<GlusterVolumeEntity> getByClusterId(Guid clusterId);

    public List<GlusterVolumeEntity> getVolumesByOption(Guid clusterId,
            GlusterStatus status,
            String optionKey,
            String optionValue);

    public List<GlusterVolumeEntity> getVolumesByStatusTypesAndOption(Guid clusterId,
            GlusterStatus status,
            List<GlusterVolumeType> volumeTypes,
            String optionKey,
            String optionValue);

    public List<GlusterVolumeEntity> getVolumesByStatusAndTypes(Guid clusterId,
            GlusterStatus status,
            List<GlusterVolumeType> volumeTypes);

    @Override
    public List<GlusterVolumeEntity> getAllWithQuery(String query);

    public void remove(Guid id);

    public void removeByClusterId(Guid clusterId);

    public void removeByName(Guid clusterId, String volName);

    public void updateVolumeStatus(Guid volumeId, GlusterStatus status);

    public void updateVolumeStatusByName(Guid clusterId, String volumeName, GlusterStatus status);

    public void addAccessProtocol(Guid volumeId, AccessProtocol protocol);

    public void removeAccessProtocol(Guid volumeId, AccessProtocol protocol);

    public void addTransportType(Guid volumeId, TransportType transportType);

    public void addTransportTypes(Guid volumeId, Collection<TransportType> transportTypes);

    public void removeTransportType(Guid volumeId, TransportType transportType);

    public void removeTransportTypes(Guid volumeId, Collection<TransportType> transportTypes);

    public void updateReplicaCount(Guid volumeId, int replicaCount);

    public void updateStripeCount(Guid volumeId, int stripeCount);

    public void updateGlusterVolume(GlusterVolumeEntity volume);

    public void updateVolumeTask(Guid volumeId, Guid taskId);

    public GlusterVolumeEntity getVolumeByGlusterTask(Guid taskId);

}
